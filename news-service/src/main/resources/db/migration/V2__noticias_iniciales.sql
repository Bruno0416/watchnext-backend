-- news-service: dos noticias de ejemplo para que la portada no arranque vacia

insert ignore into news (id, slug, title, summary, content, image_url, published_at, created_at, updated_at)
values
    (
        uuid_to_bin('7d1c2a10-0001-4c3e-9a51-000000000001'),
        'cuatro-titulos-para-tu-proxima-lista',
        'Cuatro títulos para tu próxima lista',
        'Breaking Bad, Linternas, Dexter y Endgame ya están entre nuestros guardados.',
        'Si estás armando una lista nueva, estos cuatro títulos dan para partir por caminos bien distintos. Breaking Bad y Dexter sirven para una maratón larga, Linternas es una opción más reciente y Vengadores: Endgame queda como esa película que uno termina revisitando de vez en cuando.',
        'https://image.tmdb.org/t/p/w1280/2u7zbn8EudG6kLlBzUYqP8RyFU4.jpg',
        '2026-10-01 15:00:00', now(6), now(6)
    ),
    (
        uuid_to_bin('7d1c2a10-0002-4c3e-9a51-000000000002'),
        'una-lista-para-no-perder-lo-que-quieres-ver',
        'Una lista para no perder lo que quieres ver',
        'Guardar algo para después parece mínimo, hasta que tienes cinco títulos dando vueltas.',
        'La idea de las listas en WatchNext es sencilla: encuentras algo que te tinca, lo guardas y sigues mirando. Después vuelves a tus pendientes sin tener que acordarte del nombre ni buscarlo otra vez.',
        'https://image.tmdb.org/t/p/w1280/anFx9aTOOYqgS3v7x3R84Kz67ly.jpg',
        '2026-10-03 18:00:00', now(6), now(6)
    );
