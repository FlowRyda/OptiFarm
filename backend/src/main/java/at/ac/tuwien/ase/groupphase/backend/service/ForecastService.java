package at.ac.tuwien.ase.groupphase.backend.service;

public interface ForecastService {

    /**
     * Calculates the normalized one year forecast for the given fieldId.
     * Returns -1 if no crop data is available also return -1 if all given one year forecasts are equal and no normalized
     * value can be determined.
     *
     * @param fieldId Field Id
     * @return normalized value in the range [0.0-1.0] or -1
     */
    double calculateNormalizedForecast(Long fieldId);
}
