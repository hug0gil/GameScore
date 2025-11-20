package com.gamescore.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalUsers;
    // Se elimina avgSessionTime al no tener datos de sesión
    private long newUsers;       // Usuarios registrados en los últimos 30 días
    private long churnedUsers;   // Usuarios desactivados (enabled=false) en los últimos 30 días
    private long publishedGames;
}