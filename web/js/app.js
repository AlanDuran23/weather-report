// botones tipo pildora (solo cambian el color activo)
document.querySelectorAll('[data-g]').forEach(function(g){
  g.querySelectorAll('.pill').forEach(function(p){p.onclick=function(){
    g.querySelectorAll('.pill').forEach(function(x){x.classList.remove('on')});p.classList.add('on')}})});
// ventanas (<dialog>): data-abrir="id" las muestra; data-cerrar, Escape o clic afuera (solo ajustes) las cierran
document.querySelectorAll('[data-abrir]').forEach(function(b){b.onclick=function(){
  document.getElementById(b.dataset.abrir).showModal()}});
document.querySelectorAll('[data-cerrar]').forEach(function(b){b.onclick=function(){
  b.closest('dialog').close()}});
document.querySelectorAll('dialog.modal').forEach(function(m){
  if(m.hasAttribute('data-auto'))m.showModal();  // p. ej. registro con errores al recargar
  m.addEventListener('click',function(e){if(e.target===m&&m.id==='ajustes')m.close()})});
// Ajustes: tema de color (vino, negro, gris, blanco) guardado en este navegador
var temas=document.querySelectorAll('[data-elegir]');
var marcarTema=function(){var t=document.documentElement.dataset.tema||'vino';
  temas.forEach(function(b){b.classList.toggle('on',b.dataset.elegir===t);b.setAttribute('aria-pressed',b.dataset.elegir===t)})};
temas.forEach(function(b){b.onclick=function(){document.documentElement.dataset.tema=b.dataset.elegir;
  try{localStorage.setItem('tema',b.dataset.elegir)}catch(e){}marcarTema()}});
marcarTema();
// Botones peligrosos (eliminar, restaurar): piden confirmacion
document.addEventListener('click',function(e){var b=e.target.closest('[data-confirmar]');
  if(b&&!confirm(b.dataset.confirmar))e.preventDefault()});
// Panel Principal: se refresca cada 5 s como el escritorio. Solo se reemplaza #vivo (los datos),
// asi los botones conservan el foco. index.php?parcial=1 devuelve solo <main>
var vivo=document.getElementById('vivo');
if(vivo){
  var refrescar=function(){
    fetch('index.php?parcial=1',{cache:'no-store'}).then(function(r){
      if(r.redirected){location.href=r.url;return}  // la sesion expiro
      return r.text().then(function(html){
        var tmp=document.createElement('div');tmp.innerHTML=html;
        var nuevo=tmp.querySelector('#vivo');if(!nuevo)return;
        vivo.innerHTML=nuevo.innerHTML;pintarIconos(vivo)})
    }).catch(function(){})};
  document.getElementById('refrescar').addEventListener('click',refrescar);
  setInterval(function(){if(!document.hidden)refrescar()},5000);
}
