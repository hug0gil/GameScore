-- ============================================================================
-- GAMESCORE - SCRIPT DE DATOS DE PRUEBA
-- Inserta juegos, reseñas y datos de ejemplo
-- ============================================================================

USE gamescore;

-- ============================================================================
-- LIMPIAR DATOS EXISTENTES (Opcional - Solo en desarrollo)
-- ============================================================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE email_logs;
TRUNCATE TABLE reviews;
TRUNCATE TABLE game_platforms;
TRUNCATE TABLE game_genres;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE games;
-- No truncamos users, genres, platforms porque ya tienen datos base

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- INSERTAR JUEGOS POPULARES
-- ============================================================================

INSERT INTO games (name, slug, description, cover_url, background_url, release_date, rating, metacritic, rawg_id, youtube_key, website) VALUES
(
    'The Witcher 3: Wild Hunt',
    'the-witcher-3-wild-hunt',
    'The Witcher 3: Wild Hunt es un RPG de mundo abierto de nueva generación con una apasionante historia, ambientado en un universo visualmente impresionante lleno de decisiones importantes y consecuencias de gran impacto. En The Witcher asumes el papel de un cazador de monstruos profesional, Geralt de Rivia, en búsqueda de la niña de la profecía en un vasto mundo abierto rico en ciudades comerciales, islas vikingas, cavernas peligrosas y mucho más.',
    'https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg',
    'https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg',
    '2015-05-18',
    4.66,
    92,
    3328,
    'c0i88t0Kacs',
    'https://www.thewitcher.com/en/witcher3'
),
(
    'Grand Theft Auto V',
    'grand-theft-auto-v',
    'Grand Theft Auto V es un videojuego de acción-aventura de mundo abierto desarrollado por Rockstar North. Cuando un joven estafador callejero, un ladrón de bancos retirado y un psicópata aterrador se ven envueltos con lo más aterrador y trastornado del mundo criminal, del gobierno de EE.UU y de la industria del entretenimiento, deben llevar a cabo una serie de peligrosos golpes para sobrevivir en una ciudad implacable.',
    'https://media.rawg.io/media/games/456/456dea5e1c7e3cd07060c14e96612001.jpg',
    'https://media.rawg.io/media/games/456/456dea5e1c7e3cd07060c14e96612001.jpg',
    '2013-09-17',
    4.48,
    96,
    3498,
    'QkkoHAzjnUs',
    'https://www.rockstargames.com/V/'
),
(
    'Elden Ring',
    'elden-ring',
    'Elden Ring es un juego de rol de acción desarrollado por FromSoftware. El juego es una colaboración entre el director Hidetaka Miyazaki y el novelista de fantasía George R. R. Martin. Álzate, Sinluz, y que la gracia te guíe para esgrimir el poder del Círculo de Elden y convertirte en un Señor del Círculo en las Tierras Intermedias.',
    'https://media.rawg.io/media/games/5ec/5ecac5cb026ec26a56efcc546364e348.jpg',
    'https://media.rawg.io/media/games/5ec/5ecac5cb026ec26a56efcc546364e348.jpg',
    '2022-02-25',
    4.51,
    96,
    326243,
    '47V6HP_wYf8',
    'https://en.bandainamcoent.eu/elden-ring/elden-ring'
),
(
    'Red Dead Redemption 2',
    'red-dead-redemption-2',
    'América, 1899. Arthur Morgan y la banda de Van der Linde se ven obligados a huir. Con agentes federales y los mejores cazarrecompensas de la nación pisándoles los talones, la banda debe atracar, robar y luchar para sobrevivir en el implacable territorio del corazón de América. Mientras las divisiones internas aumentan y amenazan con separarlos, Arthur debe elegir entre sus propios ideales y la lealtad a la banda que lo crió.',
    'https://media.rawg.io/media/games/511/5118aff5091cb3efec399c808f8c598f.jpg',
    'https://media.rawg.io/media/games/511/5118aff5091cb3efec399c808f8c598f.jpg',
    '2018-10-26',
    4.59,
    97,
    28,
    'gmA6MrX81z4',
    'https://www.rockstargames.com/reddeadredemption2/'
),
(
    'Cyberpunk 2077',
    'cyberpunk-2077',
    'Cyberpunk 2077 es un RPG de aventura y acción de mundo abierto ambientado en Night City, una megalópolis obsesionada con el poder, el glamur y la modificación corporal. Juegas como V, un mercenario forajido que busca un implante único que es la clave de la inmortalidad. Podrás personalizar el ciberware, el conjunto de habilidades y el estilo de juego de tu personaje.',
    'https://media.rawg.io/media/games/26d/26d4437715bee60138dab4a7c8c59c92.jpg',
    'https://media.rawg.io/media/games/26d/26d4437715bee60138dab4a7c8c59c92.jpg',
    '2020-12-10',
    4.12,
    86,
    41494,
    '8X2kIfzUeN0',
    'https://www.cyberpunk.net/'
),
(
    'The Legend of Zelda: Breath of the Wild',
    'the-legend-of-zelda-breath-of-the-wild',
    'Adéntrate en un mundo de descubrimientos, exploración y aventura en The Legend of Zelda: Breath of the Wild. Viaja a través de campos, bosques y picos de montañas mientras descubres qué le ha sucedido al reino de Hyrule en esta impresionante aventura de mundo abierto.',
    'https://media.rawg.io/media/games/cc1/cc196a5ad763955d6532cdba236f730c.jpg',
    'https://media.rawg.io/media/games/cc1/cc196a5ad763955d6532cdba236f730c.jpg',
    '2017-03-03',
    4.52,
    97,
    22511,
    'zw47_q9wbBE',
    'https://www.zelda.com/breath-of-the-wild/'
),
(
    'God of War',
    'god-of-war-2018',
    'Su venganza contra los dioses del Olimpo atrás quedó, Kratos vive ahora en el mundo de las deidades y monstruos nórdicos. En un reino hostil e implacable, debe luchar por su supervivencia mientras les enseña a su hijo a hacer lo mismo, evitando a la vez que éste repita los terribles errores del Fantasma de Esparta.',
    'https://media.rawg.io/media/games/4be/4be6a6ad0364751a96229c56bf69be59.jpg',
    'https://media.rawg.io/media/games/4be/4be6a6ad0364751a96229c56bf69be59.jpg',
    '2018-04-20',
    4.57,
    94,
    4062,
    'K0u_kAWLFf4',
    'https://www.playstation.com/games/god-of-war/'
),
(
    'Minecraft',
    'minecraft',
    'Minecraft es un juego de aventuras sandbox donde puedes construir cualquier cosa que puedas imaginar. Explora mundos generados aleatoriamente y construye cosas increíbles desde la más simple de las casas hasta el más grandioso de los castillos. Juega en modo creativo con recursos ilimitados o excava en el mundo en modo supervivencia.',
    'https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg',
    'https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg',
    '2011-11-18',
    4.42,
    93,
    22509,
    'MmB9b5njVbA',
    'https://www.minecraft.net/'
),
(
    'Hollow Knight',
    'hollow-knight',
    'Hollow Knight es un clásico juego de acción y aventuras en 2D, ambientado en un vasto mundo interconectado. Explora cavernas serpenteantes, ciudades antiguas y páramos mortales. Lucha contra criaturas corrompidas, hazte amigo de insectos extraños y resuelve antiguos misterios en el corazón del reino.',
    'https://media.rawg.io/media/games/4cf/4cfc6b7f1850590a4634b08bfab308ab.jpg',
    'https://media.rawg.io/media/games/4cf/4cfc6b7f1850590a4634b08bfab308ab.jpg',
    '2017-02-24',
    4.41,
    87,
    9767,
    'UAO2urG23S4',
    'https://www.hollowknight.com/'
),
(
    'Stardew Valley',
    'stardew-valley',
    'Has heredado la antigua parcela de tu abuelo en Stardew Valley. Armado con herramientas de segunda mano y algunas monedas, partirás para comenzar tu nueva vida. ¿Podrás aprender a vivir de la tierra y convertir estos campos cubiertos de maleza en un hogar próspero?',
    'https://media.rawg.io/media/games/713/713269608dc8f2f40f5a670a14b2de94.jpg',
    'https://media.rawg.io/media/games/713/713269608dc8f2f40f5a670a14b2de94.jpg',
    '2016-02-26',
    4.42,
    89,
    10213,
    'ot7uXNQskhs',
    'https://www.stardewvalley.net/'
),
(
    'Dark Souls III',
    'dark-souls-iii',
    'Dark Souls continúa empujando los límites con el último y ambicioso capítulo de la serie aclamada por la crítica. Sumérgete en un universo lleno de enemigos y entornos colosales. Los jugadores quedarán inmersos en un mundo de oscuridad épica a través del juego de rol de acción más rápido y el combate más intenso jamás visto en la serie.',
    'https://media.rawg.io/media/games/da1/da1b267764d77221f07a4386b6548e5a.jpg',
    'https://media.rawg.io/media/games/da1/da1b267764d77221f07a4386b6548e5a.jpg',
    '2016-04-12',
    4.49,
    89,
    1259,
    'QhWdBhTITZQ',
    'https://www.darksouls.jp/dark-souls3/'
),
(
    'Hades',
    'hades',
    'Hades es un rogue-like de mazmorras de Supergiant Games. Eres Zagreo, el inmortal Príncipe del Inframundo, y has decidido escapar del dominio de Hades. Cada vez que mueres, vuelves al comienzo, pero más fuerte y con nuevas habilidades. Combate a través de ejércitos de no-muertos en tu búsqueda de libertad.',
    'https://media.rawg.io/media/games/1f4/1f47a270b8f241e4676b14d39ec620f7.jpg',
    'https://media.rawg.io/media/games/1f4/1f47a270b8f241e4676b14d39ec620f7.jpg',
    '2020-09-17',
    4.51,
    93,
    58175,
    'Bz8l935Bv0Y',
    'https://www.supergiantgames.com/games/hades/'
);

-- ============================================================================
-- RELACIONAR JUEGOS CON GÉNEROS
-- ============================================================================

-- The Witcher 3 - RPG, Action, Adventure
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'the-witcher-3-wild-hunt' 
  AND gen.slug IN ('rpg', 'action', 'adventure');

-- GTA V - Action, Adventure, Shooter
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'grand-theft-auto-v' 
  AND gen.slug IN ('action', 'adventure', 'shooter');

-- Elden Ring - RPG, Action, Adventure
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'elden-ring' 
  AND gen.slug IN ('rpg', 'action', 'adventure');

-- Red Dead Redemption 2 - Action, Adventure, Shooter
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'red-dead-redemption-2' 
  AND gen.slug IN ('action', 'adventure', 'shooter');

-- Cyberpunk 2077 - RPG, Action, Shooter
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'cyberpunk-2077' 
  AND gen.slug IN ('rpg', 'action', 'shooter');

-- Zelda BOTW - Action, Adventure, RPG
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'the-legend-of-zelda-breath-of-the-wild' 
  AND gen.slug IN ('action', 'adventure', 'rpg');

-- God of War - Action, Adventure
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'god-of-war-2018' 
  AND gen.slug IN ('action', 'adventure');

-- Minecraft - Adventure, Indie, Simulation
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'minecraft' 
  AND gen.slug IN ('adventure', 'indie', 'simulation');

-- Hollow Knight - Action, Adventure, Platformer, Indie
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'hollow-knight' 
  AND gen.slug IN ('action', 'adventure', 'platformer', 'indie');

-- Stardew Valley - Simulation, RPG, Indie
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'stardew-valley' 
  AND gen.slug IN ('simulation', 'rpg', 'indie');

-- Dark Souls III - RPG, Action
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'dark-souls-iii' 
  AND gen.slug IN ('rpg', 'action');

-- Hades - Action, RPG, Indie
INSERT INTO game_genres (game_id, genre_id)
SELECT g.id, gen.id 
FROM games g
CROSS JOIN genres gen
WHERE g.slug = 'hades' 
  AND gen.slug IN ('action', 'rpg', 'indie');

-- ============================================================================
-- RELACIONAR JUEGOS CON PLATAFORMAS
-- ============================================================================

-- The Witcher 3 - PC, PS4, Xbox One, Switch
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'the-witcher-3-wild-hunt' 
  AND p.slug IN ('pc', 'playstation-4', 'xbox-one', 'nintendo-switch');

-- GTA V - PC, PS4, PS5, Xbox One, Xbox Series X/S
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'grand-theft-auto-v' 
  AND p.slug IN ('pc', 'playstation-4', 'playstation-5', 'xbox-one', 'xbox-series-xs');

-- Elden Ring - PC, PS4, PS5, Xbox One, Xbox Series X/S
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'elden-ring' 
  AND p.slug IN ('pc', 'playstation-4', 'playstation-5', 'xbox-one', 'xbox-series-xs');

-- Red Dead Redemption 2 - PC, PS4, Xbox One
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'red-dead-redemption-2' 
  AND p.slug IN ('pc', 'playstation-4', 'xbox-one');

-- Cyberpunk 2077 - PC, PS4, PS5, Xbox One, Xbox Series X/S
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'cyberpunk-2077' 
  AND p.slug IN ('pc', 'playstation-4', 'playstation-5', 'xbox-one', 'xbox-series-xs');

-- Zelda BOTW - Nintendo Switch
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'the-legend-of-zelda-breath-of-the-wild' 
  AND p.slug IN ('nintendo-switch');

-- God of War - PS4, PS5, PC
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'god-of-war-2018' 
  AND p.slug IN ('playstation-4', 'playstation-5', 'pc');

-- Minecraft - Todas las plataformas
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'minecraft';

-- Hollow Knight - PC, PS4, Xbox One, Switch
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'hollow-knight' 
  AND p.slug IN ('pc', 'playstation-4', 'xbox-one', 'nintendo-switch');

-- Stardew Valley - PC, PS4, Xbox One, Switch, iOS, Android
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'stardew-valley' 
  AND p.slug IN ('pc', 'playstation-4', 'xbox-one', 'nintendo-switch', 'ios', 'android');

-- Dark Souls III - PC, PS4, Xbox One
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'dark-souls-iii' 
  AND p.slug IN ('pc', 'playstation-4', 'xbox-one');

-- Hades - PC, PS4, PS5, Xbox One, Xbox Series X/S, Switch
INSERT INTO game_platforms (game_id, platform_id)
SELECT g.id, p.id 
FROM games g
CROSS JOIN platforms p
WHERE g.slug = 'hades' 
  AND p.slug IN ('pc', 'playstation-4', 'playstation-5', 'xbox-one', 'xbox-series-xs', 'nintendo-switch');

-- ============================================================================
-- INSERTAR USUARIOS ADICIONALES
-- ============================================================================

INSERT INTO users (email, name, avatar_url, provider, provider_id, role) VALUES
('juan.perez@gmail.com', 'Juan Pérez', 'https://ui-avatars.com/api/?name=Juan+Perez&background=3b82f6&color=fff', 'GOOGLE', 'google-123456', 'USER'),
('maria.garcia@outlook.com', 'María García', 'https://ui-avatars.com/api/?name=Maria+Garcia&background=ec4899&color=fff', 'GITHUB', 'github-789012', 'USER'),
('carlos.rodriguez@yahoo.com', 'Carlos Rodríguez', 'https://ui-avatars.com/api/?name=Carlos+Rodriguez&background=10b981&color=fff', 'DISCORD', 'discord-345678', 'USER'),
('ana.martinez@hotmail.com', 'Ana Martínez', 'https://ui-avatars.com/api/?name=Ana+Martinez&background=f59e0b&color=fff', 'GOOGLE', 'google-901234', 'USER'),
('pedro.sanchez@gmail.com', 'Pedro Sánchez', 'https://ui-avatars.com/api/?name=Pedro+Sanchez&background=8b5cf6&color=fff', 'LOCAL', 'local-567890', 'USER')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================================
-- INSERTAR RESEÑAS (REVIEWS)
-- ============================================================================

-- Reseña APROBADA - The Witcher 3
INSERT INTO reviews (user_id, game_id, title, content, rating, status, reviewed_by, approved_at)
SELECT 
    (SELECT id FROM users WHERE email = 'juan.perez@gmail.com'),
    (SELECT id FROM games WHERE slug = 'the-witcher-3-wild-hunt'),
    'Una obra maestra del RPG moderno',
    'The Witcher 3 es, sin duda, uno de los mejores RPGs que he jugado. La narrativa es excepcional, con personajes profundos y decisiones que realmente importan. El mundo abierto es vasto y está lleno de contenido significativo. Las misiones secundarias son tan buenas como las principales. El sistema de combate, aunque puede ser un poco repetitivo, es satisfactorio. Los gráficos son impresionantes y la banda sonora es memorable. Si eres fan de los RPGs, este juego es obligatorio.',
    10,
    'APPROVED',
    (SELECT id FROM users WHERE email = 'admin@gamescore.com'),
    NOW();

-- Reseña APROBADA - GTA V
INSERT INTO reviews (user_id, game_id, title, content, rating, status, reviewed_by, approved_at)
SELECT 
    (SELECT id FROM users WHERE email = 'maria.garcia@outlook.com'),
    (SELECT id FROM games WHERE slug = 'grand-theft-auto-v'),
    'El mejor sandbox de mundo abierto',
    'GTA V establece el estándar para los juegos de mundo abierto. Los Santos es increíblemente detallado y está lleno de vida. La historia con tres protagonistas es innovadora y funciona sorprendentemente bien. El modo online sigue siendo divertido años después del lanzamiento. La cantidad de contenido es abrumadora en el buen sentido. Los gráficos han envejecido bien y el diseño de sonido es de primera. Algunas misiones pueden ser frustrantes, pero en general es una experiencia excepcional.',
    9,
    'APPROVED',
    (SELECT id FROM users WHERE email = 'admin@gamescore.com'),
    NOW();

-- Reseña APROBADA - Elden Ring
INSERT INTO reviews (user_id, game_id, title, content, rating, status, reviewed_by, approved_at)
SELECT 
    (SELECT id FROM users WHERE email = 'carlos.rodriguez@yahoo.com'),
    (SELECT id FROM games WHERE slug = 'elden-ring'),
    'FromSoftware perfecciona su fórmula',
    'Elden Ring toma todo lo que hace grande a los juegos de FromSoftware y lo expande a un mundo abierto masivo. La libertad de exploración es refrescante comparada con los souls anteriores. Los jefes son épicos y desafiantes sin ser frustrantes. El lore creado junto con George R.R. Martin es fascinante. El diseño de mundo es magistral, con secretos en cada esquina. El combate mantiene la precisión característica de la serie. Puede ser intimidante para nuevos jugadores, pero es increíblemente gratificante.',
    10,
    'APPROVED',
    (SELECT id FROM users WHERE email = 'admin@gamescore.com'),
    NOW();

-- Reseña PENDIENTE - Cyberpunk 2077
INSERT INTO reviews (user_id, game_id, title, content, rating, status)
SELECT 
    (SELECT id FROM users WHERE email = 'ana.martinez@hotmail.com'),
    (SELECT id FROM games WHERE slug = 'cyberpunk-2077'),
    'Ambicioso pero con defectos al lanzamiento',
    'Cyberpunk 2077 es un juego que prometía revolucionar el género RPG, y aunque tiene momentos brillantes, el lanzamiento estuvo plagado de problemas técnicos. Night City es visualmente impresionante y la historia principal con Johnny Silverhand es cautivadora. Sin embargo, los bugs, los crashes y los problemas de rendimiento dañaron seriamente la experiencia inicial. Con los parches posteriores ha mejorado mucho. El sistema de progresión es profundo y las misiones secundarias son variadas. Vale la pena jugarlo ahora.',
    7,
    'PENDING';

-- Reseña APROBADA - Minecraft
INSERT INTO reviews (user_id, game_id, title, content, rating, status, reviewed_by, approved_at)
SELECT 
    (SELECT id FROM users WHERE email = 'pedro.sanchez@gmail.com'),
    (SELECT id FROM games WHERE slug = 'minecraft'),
    'Creatividad ilimitada en cubos',
    'Minecraft es un fenómeno por una buena razón. La libertad creativa es sin igual. Puedes construir prácticamente cualquier cosa que imagines. El modo supervivencia ofrece un desafío genuino, especialmente en dificultad difícil. La comunidad de mods mantiene el juego fresco año tras año. Es perfecto para jugar con amigos. Los gráficos simples pueden no impresionar a primera vista, pero tienen su encanto. El juego ha evolucionado enormemente desde su lanzamiento. Un clásico moderno que nunca pasa de moda.',
    9,
    'APPROVED',
    (SELECT id FROM users WHERE email = 'admin@gamescore.com'),
    NOW();

-- Reseña APROBADA - Hollow Knight
INSERT INTO reviews (user_id, game_id, title, content, rating, status, reviewed_by, approved_at)
SELECT 
    (SELECT id FROM users WHERE email = 'user1@test.com'),
    (SELECT id FROM games WHERE slug = 'hollow-knight'),
    'Un metroidvania excepcional',
    'Hollow Knight es una joya del género metroidvania. El diseño de niveles es laberíntico en el mejor sentido, recompensando la exploración. Los jefes son desafiantes pero justos. El arte es hermoso con un estilo único y memorable. La música ambiental crea una atmósfera melancólica perfecta. El juego es enorme, con docenas de horas de contenido por un precio ridículamente bajo. La dificultad puede ser alta, especialmente en los contenidos finales. Team Cherry creó algo verdaderamente especial aquí.',
    10,
    'APPROVED',
    (SELECT id FROM users WHERE email = 'admin@gamescore.com'),
    NOW();

-- Reseña PENDIENTE - Stardew Valley
INSERT INTO reviews (user_id, game_id, title, content, rating, status)
SELECT 
    (SELECT id FROM users WHERE email = 'user2@test.com'),
    (SELECT id FROM games WHERE slug = 'stardew-valley'),
    'Relajante y adictivo a partes iguales',
    'Stardew Valley es el juego perfecto para desconectar después de un día estresante. La mecánica de cultivo es sorprendentemente profunda. Los personajes del pueblo tienen personalidades distintivas y sus historias son conmovedoras. Hay una cantidad increíble de cosas para hacer: agricultura, pesca, minería, relaciones sociales. El juego respeta tu tiempo pero también puede absorber horas sin que te des cuenta. Hecho por una sola persona, lo cual es impresionante. Un must-play para fans de simuladores de granja.',
    9,
    'PENDING';

-- Reseña RECHAZADA - God of War
INSERT INTO reviews (user_id, game_id, title, content, rating, status, reviewed_by, review_note)
SELECT 
    (SELECT id FROM users WHERE email = 'guest@test.com'),
    (SELECT id FROM games WHERE slug = 'god-of-war-2018'),
    'No me gustó mucho',
    'El juego es aburrido, no lo recomiendo para nada.',
    3,
    'REJECTED',
    (SELECT id FROM users WHERE email = 'admin@gamescore.com'),
    'Reseña demasiado corta y sin argumentos suficientes. Por favor, proporciona más detalles sobre tu experiencia con el juego.';

-- ============================================================================
-- RESUMEN DE DATOS INSERTADOS
-- ============================================================================

SELECT '✓ Datos insertados exitosamente' AS Status;
SELECT '' AS '';
SELECT 'RESUMEN DE DATOS:' AS Info;
SELECT '==================' AS '';

SELECT CONCAT('Usuarios: ', COUNT(*)) AS Estadistica FROM users
UNION ALL SELECT CONCAT('Juegos: ', COUNT(*)) FROM games
UNION ALL SELECT CONCAT('Géneros: ', COUNT(*)) FROM genres
UNION ALL SELECT CONCAT('Plataformas: ', COUNT(*)) FROM platforms
UNION ALL SELECT CONCAT('Reseñas: ', COUNT(*)) FROM reviews

SELECT '' AS '';
SELECT 'DESGLOSE DE RESEÑAS POR ESTADO:' AS Info;
SELECT '================================' AS '';

SELECT 
    status AS Estado,
    COUNT(*) AS Cantidad
FROM reviews
GROUP BY status;