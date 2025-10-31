package com.alibaba.langengine.docusign.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RetryingHttpClient implements AutoCloseable {

    private final CloseableHttpClient delegate;
    private final int maxRetries;
    private final long baseBackoffMs;

    public RetryingHttpClient() {
        this(3, 200);
    }

    public RetryingHttpClient(int maxRetries, long baseBackoffMs) {
        this.delegate = HttpClients.createDefault();
        this.maxRetries = Math.max(0, maxRetries);
        this.baseBackoffMs = Math.max(0, baseBackoffMs);
    }

    public CloseableHttpResponse executeWithRetry(org.apache.http.client.methods.HttpUriRequest request) throws IOException {
        IOException last = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return delegate.execute(request);
            } catch (IOException e) {
                last = e;
                if (attempt == maxRetries) break;
                long sleep = baseBackoffMs * (1L << attempt);
                log.warn("HTTP attempt {} failed, retrying after {} ms: {}", attempt + 1, sleep, e.toString());
                try {
                    TimeUnit.MILLISECONDS.sleep(sleep);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw last;
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }
}


