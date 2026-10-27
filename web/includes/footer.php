    <dialog class="modal" id="ajustes" aria-labelledby="ajustes-t">
        <div class="mb">
            <h3 id="ajustes-t"><i data-i="gear"></i>Ajustes</h3>
            <label>Color de la página</label>
            <div class="temas">
                <button type="button" data-elegir="vino"><span class="muestra" style="--m1:#f3eaec"></span>Vino</button>
                <button type="button" data-elegir="negro"><span class="muestra" style="--m1:#0f0f13"></span>Negro</button>
                <button type="button" data-elegir="gris"><span class="muestra" style="--m1:#bdbdbd"></span>Gris</button>
                <button type="button" data-elegir="blanco"><span class="muestra" style="--m1:#ffffff"></span>Blanco</button>
            </div>
            <p class="note" style="margin:0 0 12px">La preferencia se guarda en este navegador.</p>
            <div class="ac"><button type="button" class="btn" data-cerrar><i data-i="x"></i>Cerrar</button></div>
        </div>
    </dialog>
    <script src="<?= recurso('js/icons.js') ?>"></script>
    <script src="<?= recurso('js/app.js') ?>"></script>
</body>
</html>
