package com.vishal.dtx.analytics.vector;

import com.vishal.dtx.analytics.model.VectorDocument;
import com.vishal.dtx.common.model.TransactionEvent;

import java.util.List;

public interface VectorRepository {

    void store(
            TransactionEvent event,
            String content,
            float[] embedding
    );

    List<VectorDocument> search(float[] queryEmbedding, int topK);
}
