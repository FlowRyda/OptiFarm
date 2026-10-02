package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.entity.ActiveField;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.entity.Task;
import at.ac.tuwien.ase.groupphase.backend.entity.TaskType;
import at.ac.tuwien.ase.groupphase.backend.repository.ActiveFieldRepository;
import at.ac.tuwien.ase.groupphase.backend.service.ForecastService;
import at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling.PriorityContext;
import at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling.PriorityScorer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static at.ac.tuwien.ase.groupphase.backend.basetest.TestData.activeFieldForScoring;
import static at.ac.tuwien.ase.groupphase.backend.basetest.TestData.defaultFarmer;
import static at.ac.tuwien.ase.groupphase.backend.basetest.TestData.fieldWithArea;
import static at.ac.tuwien.ase.groupphase.backend.basetest.TestData.seedWithBuyingPrice;
import static at.ac.tuwien.ase.groupphase.backend.basetest.TestData.taskWithTypeAndWindow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PriorityUnitTests {

    @Mock
    private ActiveFieldRepository activeFieldRepository;

    @Mock
    private ForecastService forecastService;

    @Test
    void harvesting_progressComputedFromWindowMidpoint_0_50_100_percent_andExactScores() {
        PriorityScorer sut = new PriorityScorer();
        Farmer owner = defaultFarmer();

        Field f = fieldWithArea(1L, 10.0);

        // Use relative dates so the test never becomes "overdue"
        LocalDateTime baseNow = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime start = baseNow.plusDays(1);
        LocalDateTime harvest = start.plusDays(10);

        ActiveField af = activeFieldForScoring(
            owner, f, seedWithBuyingPrice(owner, 10.0),
            30.0, start, harvest
        );

        // base = 1.0 now (double in [0..1])
        PriorityContext.PriorityContextData ctx =
            new PriorityContext.PriorityContextData(Map.of(1L, af), Map.of(1L, 1.0));

        // windowEnd in the future
        LocalDateTime we = baseNow.plusDays(30);

        LocalDateTime mid0 = start;
        LocalDateTime mid50 = start.plusDays(5);
        LocalDateTime mid100 = harvest;

        Task t0 = taskWithTypeAndWindow(f, TaskType.HARVESTING, wsForMidpoint(mid0, we), we);
        Task t50 = taskWithTypeAndWindow(f, TaskType.HARVESTING, wsForMidpoint(mid50, we), we);
        Task t100 = taskWithTypeAndWindow(f, TaskType.HARVESTING, wsForMidpoint(mid100, we), we);

        long s0 = sut.score(t0, ctx);
        long s50 = sut.score(t50, ctx);
        long s100 = sut.score(t100, ctx);

        assertTrue(s0 < s50);
        assertTrue(s50 < s100);

        // If you want exact expected values, consider injecting a Clock into PriorityScorer.
        // Without that, urgency depends on "now", so use a tolerance:
        LocalDateTime nowForExpected = LocalDateTime.now();
        double urgency = expectedUrgency(we, nowForExpected);

        assertEquals(expectedHarvestScore(0.0, urgency), s0, 50_000);
        assertEquals(expectedHarvestScore(0.5, urgency), s50, 50_000);
        assertEquals(expectedHarvestScore(1.0, urgency), s100, 50_000);
    }

    @Test
    void progressUsesWindowEndIfNoWindowStart_exact50PercentScore() {
        PriorityScorer sut = new PriorityScorer();
        Farmer owner = defaultFarmer();

        Field f = fieldWithArea(1L, 10.0);

        LocalDateTime baseNow = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime start = baseNow.plusDays(1);
        LocalDateTime harvest = start.plusDays(10);

        ActiveField af = activeFieldForScoring(
            owner, f, seedWithBuyingPrice(owner, 10.0),
            30.0, start, harvest
        );

        PriorityContext.PriorityContextData ctx =
            new PriorityContext.PriorityContextData(Map.of(1L, af), Map.of(1L, 1.0));

        // ws=null, we=start+5d => reference=we => exactly 50%
        LocalDateTime we = start.plusDays(5);
        Task t = taskWithTypeAndWindow(f, TaskType.HARVESTING, null, we);

        long actual = sut.score(t, ctx);

        LocalDateTime nowForExpected = LocalDateTime.now();
        long expected = expectedHarvestScore(0.5, expectedUrgency(we, nowForExpected));

        // urgency depends on "now", allow tolerance unless you inject a Clock
        assertEquals(expected, actual, 50_000);
    }

    @Test
    void harvesting_withoutActiveField_returns0() {
        PriorityScorer sut = new PriorityScorer();

        Field f = fieldWithArea(1L, 10.0);
        PriorityContext.PriorityContextData ctx =
            new PriorityContext.PriorityContextData(Map.of(), Map.of(1L, 1.0));

        LocalDateTime baseNow = LocalDateTime.now().withSecond(0).withNano(0);

        Task t = taskWithTypeAndWindow(
            f,
            TaskType.HARVESTING,
            baseNow.plusDays(1),
            baseNow.plusDays(2)
        );

        assertEquals(0L, sut.score(t, ctx));
    }

    @Test
    void priorityContext_activeFieldsUseProfit_forecastUsedOtherwise_areaFallbackIfNoForecast() {
        PriorityContext sut = new PriorityContext();
        Farmer owner = defaultFarmer();

        Field f1 = fieldWithArea(1L, 10.0);
        Field f2 = fieldWithArea(2L, 10.0);
        Field f3 = fieldWithArea(3L, 10.0);

        List<Task> tasks = List.of(
            taskWithTypeAndWindow(f1, TaskType.NONE, null, null),
            taskWithTypeAndWindow(f2, TaskType.NONE, null, null),
            taskWithTypeAndWindow(f3, TaskType.NONE, null, null)
        );

        LocalDateTime baseNow = LocalDateTime.now().withSecond(0).withNano(0);

        ActiveField af1 = activeFieldForScoring(
            owner, f1, seedWithBuyingPrice(owner, 10.0),
            20.0,
            baseNow.minusDays(20),
            baseNow.minusDays(10)
        );

        when(activeFieldRepository.findByFieldIdAndEndDateIsNull(1L)).thenReturn(Optional.of(af1));
        when(activeFieldRepository.findByFieldIdAndEndDateIsNull(2L)).thenReturn(Optional.empty());
        when(activeFieldRepository.findByFieldIdAndEndDateIsNull(3L)).thenReturn(Optional.empty());

        when(forecastService.calculateNormalizedForecast(2L)).thenReturn(0.8);
        when(forecastService.calculateNormalizedForecast(3L)).thenReturn(-1.0); // "no forecast"

        PriorityContext.PriorityContextData ctx =
            sut.buildPriorityContext(tasks, activeFieldRepository, forecastService);

        assertTrue(ctx.hasActiveField(1L));
        assertFalse(ctx.hasActiveField(2L));
        assertFalse(ctx.hasActiveField(3L));

        assertEquals(0.8, ctx.baseValueForFieldId(2L), 1e-9);

        // only one profit value -> bounds collapse -> normalizeWithBounds => 0.5
        assertEquals(0.5, ctx.baseValueForFieldId(1L), 1e-9);

        // area fallback expected ≈ 0.4976955262...
        assertEquals(0.49769552621700486, ctx.baseValueForFieldId(3L), 1e-6);
    }

    @Test
    void maintenance_progressStages_earlyHigherThanLate() {
        PriorityScorer sut = new PriorityScorer();

        Farmer owner = defaultFarmer();
        Field f = fieldWithArea(1L, 10.0);

        LocalDateTime baseNow = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime start = baseNow.plusDays(1);
        LocalDateTime harvest = start.plusDays(30);

        ActiveField af = activeFieldForScoring(
            owner,
            f,
            seedWithBuyingPrice(owner, 10.0),
            30.0,
            start,
            harvest
        );

        PriorityContext.PriorityContextData ctx = new PriorityContext.PriorityContextData(
            Map.of(1L, af),
            Map.of(1L, 1.0)
        );

        LocalDateTime we = baseNow.plusDays(45);

        // Stage 1: 20% progress (early) - should use inv^3
        LocalDateTime midEarly = start.plusDays(6);
        Task tEarly = taskWithTypeAndWindow(f, TaskType.MAINTENANCE, wsForMidpoint(midEarly, we), we);

        // Stage 2: 50% progress (middle) - should use inv^2
        LocalDateTime midMiddle = start.plusDays(15);
        Task tMiddle = taskWithTypeAndWindow(f, TaskType.MAINTENANCE, wsForMidpoint(midMiddle, we), we);

        // Stage 3: 80% progress (late) - should use inv^1
        LocalDateTime midLate = start.plusDays(24);
        Task tLate = taskWithTypeAndWindow(f, TaskType.MAINTENANCE, wsForMidpoint(midLate, we), we);

        long sEarly = sut.score(tEarly, ctx);
        long sMiddle = sut.score(tMiddle, ctx);
        long sLate = sut.score(tLate, ctx);

        // Maintenance should be highest priority early, lowest late
        assertTrue(sEarly > sMiddle, "Early stage maintenance should score higher than middle stage");
        assertTrue(sMiddle > sLate, "Middle stage maintenance should score higher than late stage");

        // Verify the early stage has significantly higher priority
        assertTrue(sEarly > sLate * 2, "Early maintenance should be substantially more important than late");
    }

    // ---------------- Helpers ----------------

    private static LocalDateTime wsForMidpoint(LocalDateTime mid, LocalDateTime we) {
        long deltaSeconds = Duration.between(we, mid).getSeconds();
        return mid.plusSeconds(deltaSeconds);
    }

    private static double expectedUrgency(LocalDateTime windowEnd, LocalDateTime now) {
        if (windowEnd == null) return 1.0;

        long minutesLeft = Duration.between(now, windowEnd).toMinutes();
        if (minutesLeft <= 0) return 1.0; // match your "safety net"

        double hoursLeft = minutesLeft / 60.0;
        return 1.0 + 1.0 / (1.0 + (hoursLeft / 24.0));
    }

    private static long expectedHarvestScore(double prog01, double urgency) {
        double business = 1_200_000.0 * Math.pow(prog01, 3);
        double bonus = 3_000_000.0;
        return Math.round((business + bonus) * urgency);
    }
}