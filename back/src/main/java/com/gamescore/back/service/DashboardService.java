package com.gamescore.back.service;

import com.gamescore.back.model.DTOs.ChartDataDto;
import com.gamescore.back.model.DTOs.DashboardStatsDto;
import com.gamescore.back.repository.GameRepository;
import com.gamescore.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    /**
     * Recopila todas las estadísticas para las tarjetas del dashboard.
     */
    public DashboardStatsDto getDashboardStats() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        long totalUsers = userRepository.count();
        long publishedGames = gameRepository.count();
        long newUsers = userRepository.countByCreatedAtAfter(thirtyDaysAgo);
        
        // Asumimos que un "usuario de baja" (churned) es aquel cuya cuenta
        // fue desactivada (enabled=false) en los últimos 30 días.
        long churnedUsers = userRepository.countByEnabledFalseAndUpdatedAtAfter(thirtyDaysAgo);

        return DashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .newUsers(newUsers)
                .churnedUsers(churnedUsers)
                .publishedGames(publishedGames)
                .build();
    }

    /**
     * Genera los datos para el gráfico de nuevos usuarios de los últimos 6 meses.
     */
    public ChartDataDto getNewUserChartData() {
        List<String> labels = new ArrayList<>();
        List<Long> dataPoints = new ArrayList<>();
        // Formato para los nombres de los meses en español (ej. "Ene", "Feb")
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", new Locale("es", "ES"));

        // Itera sobre los últimos 6 meses (incluyendo el actual)
        for (int i = 5; i >= 0; i--) {
            YearMonth currentMonth = YearMonth.now().minusMonths(i);

            LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

            // Usa el método de conteo eficiente
            long userCount = userRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);

            // Añade la etiqueta del mes (ej. "Ene.") y el dato
            String monthLabel = monthFormatter.format(currentMonth);
            labels.add(monthLabel.substring(0, 1).toUpperCase() + monthLabel.substring(1));
            dataPoints.add(userCount);
        }

        return new ChartDataDto(labels, dataPoints);
    }
}