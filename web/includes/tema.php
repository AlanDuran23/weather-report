<?php if (!function_exists('e')) { http_response_code(404); exit; } ?>
<script>/* aplica el tema guardado antes de pintar la pagina (evita el parpadeo) */try{document.documentElement.dataset.tema=localStorage.getItem('tema')||'vino'}catch(e){}</script>
