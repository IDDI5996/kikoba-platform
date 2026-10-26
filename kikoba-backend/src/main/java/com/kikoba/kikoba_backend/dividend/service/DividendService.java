package com.kikoba.kikoba_backend.dividend.service;

import com.kikoba.kikoba_backend.dividend.dto.CalculateDividendRequest;
import com.kikoba.kikoba_backend.dividend.dto.DividendPayoutResponse;
import com.kikoba.kikoba_backend.dividend.dto.DividendResponse;

import java.util.List;
import java.util.UUID;

public interface DividendService {

    DividendResponse calculateDividend(UUID groupId, UUID requesterId, CalculateDividendRequest request);
    DividendResponse approveDividend(UUID dividendId, UUID requesterId);
    DividendResponse distributeDividend(UUID dividendId, UUID requesterId);
    DividendResponse cancelDividend(UUID dividendId, UUID requesterId, String reason);

    DividendResponse getDividendById(UUID dividendId, UUID requesterId);
    List<DividendResponse> getGroupDividends(UUID groupId, UUID requesterId);
    List<DividendPayoutResponse> getDividendPayouts(UUID dividendId, UUID requesterId);
    List<DividendPayoutResponse> getMyDividends(UUID userId);

    DividendPayoutResponse confirmPayout(UUID payoutId, UUID requesterId, String reference);
}