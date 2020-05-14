package com.mall.shopping.services;

import com.mall.shopping.ICartService;
import com.mall.shopping.converter.CartItemConverter;
import com.mall.shopping.dal.entitys.Item;
import com.mall.shopping.dal.persistence.ItemMapper;
import com.mall.shopping.dto.*;
import com.mall.user.IMemberService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.QueryMemberRequest;
import com.mall.user.dto.QueryMemberResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * User：zhouchen
 * Time: 2020/5/13  14:45
 * Description:
 */
@Service(cluster = "failfast")
@Component
@Slf4j
public class ICartServiceImpl implements ICartService {
    @Autowired
    private RedissonClient redissonClient;

    @Reference
    IMemberService memberService;

    @Autowired
    ItemMapper itemMapper;

    /**
     * 根据用户id来获取所有的购物车信息
     * @param request
     * @return
     */
    @Override
    public CartListByIdResponse getCartListById(CartListByIdRequest request) {
        CartListByIdResponse cartListByIdResponse = new CartListByIdResponse();
        Long userId = request.getUserId();
        //从用户表中获取对应的用户信息
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setUserId(userId);
        QueryMemberResponse user = memberService.queryMemberById(queryMemberRequest);

        //从redis中获取该用户对应的购物车信息
        RMap<Object, Object> map = redissonClient.getMap("cart");
        List<CartProductDto> cartProductDtoList = (List<CartProductDto>) map.get(user.getUsername());
        if (cartProductDtoList == null) {
            cartProductDtoList = new ArrayList<>();
        }

        cartListByIdResponse.setCartProductDtos(cartProductDtoList);
        return cartListByIdResponse;
    }

    /**
     * 添加商品到购物车
     * @param request
     * @return
     */
    @Override
    public AddCartResponse addToCart(AddCartRequest request) {
        AddCartResponse addCartResponse = new AddCartResponse();
        Long productId = request.getItemId();
        Long userId = request.getUserId();
        Integer productNum = request.getNum();

        //首先从item表中获取该商品所对应的详细信息
        Item item = itemMapper.selectByPrimaryKey(productId);

        //构建购物车商品信息
        CartProductDto cartProductDto = CartItemConverter.item2Dto(item);
        cartProductDto.setChecked("false");
        cartProductDto.setProductNum(Long.valueOf(productNum));

        //从数据库中获取当前用户的信息
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setUserId(userId);
        QueryMemberResponse member = memberService.queryMemberById(queryMemberRequest);

        //将新生成的购物车添加到redis中
        RMap<Object, Object> cart = redissonClient.getMap("cart");
        List<CartProductDto> cartList = (List<CartProductDto>) cart.get(member.getUsername());
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        //用来标记是否购物车中是否已经包含了要添加的商品
        boolean flag = false;
        for (CartProductDto productDto : cartList) {
            if (productDto.getProductId().equals(productId)) {
                //购物车中包含新商品，更新商品数量
                productDto.setProductNum(productDto.getProductNum() + productNum);
                flag = true;
                break;
            }
        }
        if (!flag) {
            cartList.add(cartProductDto);
        }
        cart.put(member.getUsername(),cartList);

        addCartResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        addCartResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        return addCartResponse;
    }

    /**
     * 更新购物车中的商品的数量
     * @param request
     * @return
     */
    @Override
    public UpdateCartNumResponse updateCartNum(UpdateCartNumRequest request) {
        UpdateCartNumResponse updateCartNumResponse = new UpdateCartNumResponse();
        String checked = request.getChecked();
        Long productId = request.getItemId();
        Integer productNum = request.getNum();
        Long userId = request.getUserId();

        //获取用户信息
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setUserId(userId);
        QueryMemberResponse member = memberService.queryMemberById(queryMemberRequest);

        //获取购物车商品列表
        RMap<Object, Object> cart = redissonClient.getMap("cart");
        List<CartProductDto> cartProductDtos = (List<CartProductDto>) cart.get(member.getUsername());

        //遍历购物车商品，更改指定productId的相关信息
        for (CartProductDto cartProductDto : cartProductDtos) {
            if (cartProductDto.getProductId().equals(productId)) {
                cartProductDto.setProductNum(Long.valueOf(productNum));
                cartProductDto.setChecked(checked);
            }
        }
        cart.put(member.getUsername(), cartProductDtos);

        updateCartNumResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        updateCartNumResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        return updateCartNumResponse;
    }

    /**
     * 暂时用不到
     * @param request
     * @return
     */
    @Override
    public CheckAllItemResponse checkAllCartItem(CheckAllItemRequest request) {
        return null;
    }

    /**
     * 从购物车中删除指定的商品
     * @param request
     * @return
     */
    @Override
    public DeleteCartItemResponse deleteCartItem(DeleteCartItemRequest request) {
        DeleteCartItemResponse deleteCartItemResponse = new DeleteCartItemResponse();
        Long userId = request.getUserId();
        Long productId = request.getItemId();

        //获取用户信息
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setUserId(userId);
        QueryMemberResponse member = memberService.queryMemberById(queryMemberRequest);

        RMap<Object, Object> cart = redissonClient.getMap("cart");
        List<CartProductDto> cartProductDtos = (List<CartProductDto>) cart.get(member.getUsername());

        //遍历商品列表
        for (int i = 0; i < cartProductDtos.size(); i++) {
            if (cartProductDtos.get(i).getProductId().equals(productId)) {
                cartProductDtos.remove(i);
                break;
            }
        }
        cart.put(member.getUsername(), cartProductDtos);

        deleteCartItemResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        deleteCartItemResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());

        return deleteCartItemResponse;
    }

    /**
     * 删除购物车中选中的商品
     * @param request
     * @return
     */
    @Override
    public DeleteCheckedItemResposne deleteCheckedItem(DeleteCheckedItemRequest request) {
        DeleteCheckedItemResposne deleteCheckedItemResposne = new DeleteCheckedItemResposne();
        Long userId = request.getUserId();

        //获取用户信息
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setUserId(userId);
        QueryMemberResponse member = memberService.queryMemberById(queryMemberRequest);

        RMap<Object, Object> cart = redissonClient.getMap("cart");
        List<CartProductDto> cartProductDtos = (List<CartProductDto>) cart.get(member.getUsername());
        //遍历商品列表
        List<CartProductDto> deletes = new ArrayList<>();
        for (CartProductDto cartProductDto : cartProductDtos) {
            if ("true".equals(cartProductDto.getChecked())) {
                deletes.add(cartProductDto);
            }
        }
        cartProductDtos.removeAll(deletes);
        cart.put(member.getUsername(), cartProductDtos);

        deleteCheckedItemResposne.setCode(SysRetCodeConstants.SUCCESS.getCode());
        deleteCheckedItemResposne.setMsg(SysRetCodeConstants.SUCCESS.getMessage());

        return deleteCheckedItemResposne;
    }

    /**
     * 暂时用不到
     * @param request
     * @return
     */
    @Override
    public ClearCartItemResponse clearCartItemByUserID(ClearCartItemRequest request) {
        return null;
    }

}
