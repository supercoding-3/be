package com.github.p3.mapper;

import com.github.p3.dto.ChatRoomListDto;
import com.github.p3.entity.Product;
import com.github.p3.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatRoomMapper {
    @Mapping(source = "transactionId", target = "chatRoomId")
    @Mapping(source = "product.title", target = "productName")
    @Mapping(source = "transactionPrice", target = "productPrice")
    @Mapping(source = "product", target = "productProfileImageUrl") // 첫 번째 이미지 불러오기
    ChatRoomListDto toDto(Transaction transaction);

    // 상품의 첫 번째 이미지를 반환하는 메서드
    default String mapProductProfileImageUrl(Product product) {
        if (product != null && product.getImages() != null && !product.getImages().isEmpty()) {
            return product.getImages().get(0).getImageUrl(); // 첫 번째 이미지를 반환
        }
        return null; // 이미지가 없으면 null 반환
    }

    default ChatRoomListDto toDtoWithOppositeProfile(Transaction transaction, Integer currentUserId) {
        boolean isBuyer = transaction.getBuyer().getUserId().equals(currentUserId);

        String oppositeNickname = isBuyer ? transaction.getSeller().getUserNickname() : transaction.getBuyer().getUserNickname();
        String oppositeProfileImageUrl = isBuyer ? transaction.getSeller().getProfileImageUrl() : transaction.getBuyer().getProfileImageUrl();

        return new ChatRoomListDto(
                transaction.getTransactionId(),
                transaction.getProduct().getTitle(),
                transaction.getTransactionPrice(),
                mapProductProfileImageUrl(transaction.getProduct()),
                oppositeNickname,
                oppositeProfileImageUrl
        );
    }
}
