package com.kidmily.algoga_server.notification.application.service;

import com.kidmily.algoga_server.notification.domain.model.Notification;
import com.kidmily.algoga_server.notification.domain.model.NotificationType;
import com.kidmily.algoga_server.notification.domain.repository.NotificationRepository;
import com.kidmily.algoga_server.notification.exception.NotificationErrorCode;
import com.kidmily.algoga_server.notification.exception.NotificationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceTest {

    @InjectMocks
    private NotificationCommandService notificationCommandService;

    @Mock
    private NotificationRepository notificationRepository;

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_ID = 2L;
    private static final Long NOTIFICATION_ID = 100L;

    private Notification notificationOf(Long userId) {
        return Notification.reconstitute(
                NOTIFICATION_ID, userId, NotificationType.QNA_ANSWERED,
                "메시지", "상세", 10L, false, LocalDateTime.now()
        );
    }

    // ===== 개별 읽음 =====

    @Test
    @DisplayName("본인 알림을 읽음 처리하면 저장된다.")
    void markAsRead_success() {
        // given
        given(notificationRepository.findById(NOTIFICATION_ID))
                .willReturn(Optional.of(notificationOf(OWNER_ID)));

        // when
        notificationCommandService.markAsRead(OWNER_ID, NOTIFICATION_ID);

        // then
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("존재하지 않는 알림을 읽음 처리하면 NOTIFICATION_NOT_FOUND 예외가 발생한다.")
    void markAsRead_notFound_throws() {
        // given
        given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationCommandService.markAsRead(OWNER_ID, NOTIFICATION_ID))
                .isInstanceOf(NotificationException.class)
                .hasMessage(NotificationErrorCode.NOTIFICATION_NOT_FOUND.getMessage());

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("타인의 알림을 읽음 처리하면 NOTIFICATION_UNAUTHORIZED 예외가 발생한다.")
    void markAsRead_unauthorized_throws() {
        // given — 알림 소유자는 OWNER_ID인데 OTHER_ID가 요청
        given(notificationRepository.findById(NOTIFICATION_ID))
                .willReturn(Optional.of(notificationOf(OWNER_ID)));

        // when & then
        assertThatThrownBy(() -> notificationCommandService.markAsRead(OTHER_ID, NOTIFICATION_ID))
                .isInstanceOf(NotificationException.class)
                .hasMessage(NotificationErrorCode.NOTIFICATION_UNAUTHORIZED.getMessage());

        verify(notificationRepository, never()).save(any());
    }

    // ===== 전체 읽음 =====

    @Test
    @DisplayName("전체 읽음 처리를 하면 레포지토리에 위임한다.")
    void markAllAsRead_success() {
        // when
        notificationCommandService.markAllAsRead(OWNER_ID);

        // then
        verify(notificationRepository, times(1)).markAllAsRead(OWNER_ID);
    }

    // ===== 개별 삭제 =====

    @Test
    @DisplayName("본인 알림을 삭제하면 레포지토리에서 제거한다.")
    void deleteNotification_success() {
        // given
        given(notificationRepository.findById(NOTIFICATION_ID))
                .willReturn(Optional.of(notificationOf(OWNER_ID)));

        // when
        notificationCommandService.deleteNotification(OWNER_ID, NOTIFICATION_ID);

        // then
        verify(notificationRepository, times(1)).deleteById(NOTIFICATION_ID);
    }

    @Test
    @DisplayName("존재하지 않는 알림을 삭제하면 NOTIFICATION_NOT_FOUND 예외가 발생한다.")
    void deleteNotification_notFound_throws() {
        // given
        given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationCommandService.deleteNotification(OWNER_ID, NOTIFICATION_ID))
                .isInstanceOf(NotificationException.class)
                .hasMessage(NotificationErrorCode.NOTIFICATION_NOT_FOUND.getMessage());

        verify(notificationRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("타인의 알림을 삭제하면 NOTIFICATION_UNAUTHORIZED 예외가 발생한다.")
    void deleteNotification_unauthorized_throws() {
        // given
        given(notificationRepository.findById(NOTIFICATION_ID))
                .willReturn(Optional.of(notificationOf(OWNER_ID)));

        // when & then
        assertThatThrownBy(() -> notificationCommandService.deleteNotification(OTHER_ID, NOTIFICATION_ID))
                .isInstanceOf(NotificationException.class)
                .hasMessage(NotificationErrorCode.NOTIFICATION_UNAUTHORIZED.getMessage());

        verify(notificationRepository, never()).deleteById(anyLong());
    }

    // ===== 전체 삭제 =====

    @Test
    @DisplayName("전체 삭제를 하면 레포지토리에 위임한다.")
    void deleteAllNotifications_success() {
        // when
        notificationCommandService.deleteAllNotifications(OWNER_ID);

        // then
        verify(notificationRepository, times(1)).deleteAllByUserId(OWNER_ID);
    }
}