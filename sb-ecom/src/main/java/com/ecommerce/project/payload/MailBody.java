package com.ecommerce.project.payload;

import lombok.Builder;

@Builder
public record MailBody(String to, String subject, String text) {
}
