/**
 * Shine viewport engine — locks the UI shell to the exact visible area.
 * Android WebView pushes pixel size from native; browsers use visualViewport.
 */
(function (global) {
  'use strict';

  function setViewport(w, h, top, right, bottom, left) {
    var doc = global.document;
    if (!doc || !doc.documentElement) return;
    var r = doc.documentElement;
    w = Math.max(0, Math.round(w));
    h = Math.max(0, Math.round(h));
    top = Math.max(0, Math.round(top || 0));
    right = Math.max(0, Math.round(right || 0));
    bottom = Math.max(0, Math.round(bottom || 0));
    left = Math.max(0, Math.round(left || 0));
    r.style.setProperty('--app-w', w + 'px');
    r.style.setProperty('--app-h', h + 'px');
    r.style.setProperty('--sat', top + 'px');
    r.style.setProperty('--sar', right + 'px');
    r.style.setProperty('--sab', bottom + 'px');
    r.style.setProperty('--sal', left + 'px');
    r.classList.add('shine-viewport-ready');
    if (global.ShineAndroid) r.classList.add('shine-android');
  }

  function fromVisualViewport() {
    var vv = global.visualViewport;
    if (!vv) {
      setViewport(global.innerWidth, global.innerHeight, 0, 0, 0, 0);
      return;
    }
    var top = vv.offsetTop;
    var left = vv.offsetLeft;
    var bottom = Math.max(0, global.innerHeight - vv.height - top);
    setViewport(vv.width, vv.height, top, 0, bottom, left);
  }

  global.__shineViewport = { set: setViewport, refresh: fromVisualViewport };

  if (global.visualViewport) {
    global.visualViewport.addEventListener('resize', fromVisualViewport);
    global.visualViewport.addEventListener('scroll', fromVisualViewport);
  }
  global.addEventListener('resize', fromVisualViewport);
  global.addEventListener('orientationchange', function () {
    setTimeout(fromVisualViewport, 120);
  });

  if (typeof global.ShineAndroid === 'undefined') {
    if (global.document.readyState === 'loading') {
      global.document.addEventListener('DOMContentLoaded', fromVisualViewport);
    } else {
      fromVisualViewport();
    }
  }
})(window);
