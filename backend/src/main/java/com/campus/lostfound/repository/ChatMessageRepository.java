package com.campus.lostfound.repository;

import com.campus.lostfound.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.fromUserId = :userId1 AND m.toUserId = :userId2) OR " +
           "(m.fromUserId = :userId2 AND m.toUserId = :userId1) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversationMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query(value = "SELECT * FROM chat_message WHERE " +
           "(from_user_id = :userId OR to_user_id = :userId) " +
           "ORDER BY created_at DESC", nativeQuery = true)
    List<ChatMessage> findAllUserMessages(@Param("userId") Long userId);

    int countByToUserIdAndIsRead(Long toUserId, Integer isRead);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = 1 WHERE m.fromUserId = :fromUserId AND m.toUserId = :toUserId")
    void markAsRead(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
}