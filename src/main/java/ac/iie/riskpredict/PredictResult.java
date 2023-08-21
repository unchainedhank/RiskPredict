package ac.iie.riskpredict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author a3
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictResult {
    private String contributions;
    private double fulfillProb;
}
