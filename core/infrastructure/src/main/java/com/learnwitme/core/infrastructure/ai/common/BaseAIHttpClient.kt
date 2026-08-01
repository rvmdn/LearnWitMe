package com.learnwitme.core.infrastructure.ai.common

import com.learnwitme.core.infrastructure.ai.AIHttpClient
import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AITransportRequestDto
import com.learnwitme.core.infrastructure.ai.AITransportResponseDto
import com.learnwitme.core.infrastructure.ai.ProviderExceptionMapper

/**
 * Abstract base class owning shared transport execution, retry handling, and exception translation for AI HTTP clients.
 */
abstract class BaseAIHttpClient(
    protected val providerMetadata: ProviderMetadata,
    protected val exceptionMapper: ProviderExceptionMapper = ProviderExceptionMapper(),
    protected val headerBuilder: HttpHeaderBuilder = HttpHeaderBuilder(),
    protected val responseParser: HttpResponseParser = HttpResponseParser(),
    protected val retryExecutor: RetryExecutor = RetryExecutor()
) : AIHttpClient {

    abstract fun buildRequestBodyJson(requestDto: AITransportRequestDto): String

    abstract fun executeTransportCall(
        config: AIProviderConfiguration,
        requestDto: AITransportRequestDto,
        headers: Map<String, String>,
        bodyJson: String
    ): TransportResult

    override fun execute(
        config: AIProviderConfiguration,
        requestDto: AITransportRequestDto
    ): AITransportResponseDto {
        // 1. Api Key Check
        if (config.apiKey.isBlank()) {
            throw exceptionMapper.mapToDomainException(
                statusCode = 401,
                message = "API key is missing for provider ${providerMetadata.providerName}",
                provider = providerMetadata.providerName
            )
        }

        // 2. Build Headers & Body
        val headers = headerBuilder.buildHeaders(
            apiKey = config.apiKey,
            additionalHeaders = config.additionalHeaders
        )
        val bodyJson = buildRequestBodyJson(requestDto)

        // 3. Execute with Retry
        return retryExecutor.executeWithRetry(maxRetries = config.maxRetries) {
            when (val result = executeTransportCall(config, requestDto, headers, bodyJson)) {
                is TransportResult.Success -> result.responseDto
                is TransportResult.Failure -> {
                    throw exceptionMapper.mapToDomainException(
                        statusCode = result.statusCode,
                        message = result.message,
                        provider = providerMetadata.providerName,
                        cause = result.cause
                    )
                }
            }
        }
    }
}
