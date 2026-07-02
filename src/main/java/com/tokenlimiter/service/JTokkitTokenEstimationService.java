package com.tokenlimiter.service;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.ModelType;
import com.tokenlimiter.dto.request.EstimateTokensRequest;
import com.tokenlimiter.dto.response.EstimateTokensResponse;
import com.tokenlimiter.exception.InvalidModelException;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * JTokkit-based implementation of token estimation.
 * Counting is performed locally on the JVM without external API calls.
 */
@Service
public class JTokkitTokenEstimationService implements TokenEstimationService {

    private static final Map<String, String> MODEL_ALIASES = Map.of(
            "gpt-4o", ModelType.GPT_4O.getName(),
            "gpt-4o-mini", ModelType.GPT_4O_MINI.getName(),
            "gpt-4", ModelType.GPT_4.getName(),
            "gpt-3.5-turbo", ModelType.GPT_3_5_TURBO.getName());

    private final EncodingRegistry encodingRegistry;

    public JTokkitTokenEstimationService() {
        this.encodingRegistry = Encodings.newDefaultEncodingRegistry();
    }

    /**
     * {@inheritDoc}
     *
     * @throws InvalidModelException if the model is not supported
     */
    @Override
    public EstimateTokensResponse estimateTokens(EstimateTokensRequest request) {
        try {
            Encoding encoding = resolveEncoding(request.targetModel());
            int tokenCount = encoding.countTokens(request.inputText());
            return new EstimateTokensResponse((long) tokenCount, request.targetModel());
        } catch (IllegalArgumentException ex) {
            throw unsupportedModel(request.targetModel());
        }
    }

    /**
     * Resolves the BPE encoding for the given model name or encoding type.
     */
    private Encoding resolveEncoding(String targetModel) {
        String normalizedModel = targetModel.trim().toLowerCase();

        if (MODEL_ALIASES.containsKey(normalizedModel)) {
            return encodingRegistry
                    .getEncodingForModel(MODEL_ALIASES.get(normalizedModel))
                    .orElseThrow(() -> unsupportedModel(targetModel));
        }

        Optional<Encoding> encodingByModel = encodingRegistry.getEncodingForModel(normalizedModel);
        if (encodingByModel.isPresent()) {
            return encodingByModel.get();
        }

        Optional<EncodingType> encodingType = EncodingType.fromName(normalizedModel);
        if (encodingType.isPresent()) {
            return encodingRegistry.getEncoding(encodingType.get());
        }

        throw unsupportedModel(targetModel);
    }

    private InvalidModelException unsupportedModel(String targetModel) {
        return new InvalidModelException(
                targetModel,
                "Unsupported target model: '" + targetModel + "'. Supported examples: gpt-4o, gpt-3.5-turbo");
    }
}
