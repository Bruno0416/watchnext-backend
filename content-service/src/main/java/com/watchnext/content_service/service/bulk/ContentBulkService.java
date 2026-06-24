package com.watchnext.content_service.service.bulk;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.dto.internal.ContentBasicDetail;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ContentBulkService {
    Mono<List<ContentBasicDetail>> fetchBulkContent(
        List<ContentRefRequest> requests,
        String language
    );
}
