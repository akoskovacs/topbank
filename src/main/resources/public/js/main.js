window.onload = (function() {
   // Az előző oldal beállítása
   document.cookie = ("prev_page=" + window.location.pathname + "; path=/; " + document.cookie);
});