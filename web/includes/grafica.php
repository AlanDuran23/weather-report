<?php
// Grafica de lineas en SVG (reemplaza a JFreeChart del escritorio)

// Escala "bonita" para el eje Y: devuelve [min, max, paso]
function escala_y(float $min, float $max, int $marcas = 5): array
{
    if ($min == $max) {
        $min -= 1;
        $max += 1;
    }
    $rango = $max - $min;
    $min -= $rango * 0.06;   // margenes como setLowerMargin/UpperMargin
    $max += $rango * 0.15;
    $bruto = ($max - $min) / $marcas;
    $mag = 10 ** floor(log10($bruto));
    foreach ([1, 2, 2.5, 5, 10] as $f) {
        if ($f * $mag >= $bruto) {
            $paso = $f * $mag;
            break;
        }
    }
    return [floor($min / $paso) * $paso, ceil($max / $paso) * $paso, $paso];
}

// Mini grafica de las ultimas lecturas para el Panel; usa el color del texto (currentColor).
// $valores: del mas antiguo al mas reciente
function svg_mini(array $valores): string
{
    $valores = array_values(array_filter($valores, fn($v) => $v !== null));
    $n = count($valores);
    if ($n < 2) {
        return '';
    }
    $W = 240; $H = 48; $m = 5;
    $min = min($valores);
    $rango = (max($valores) - $min) ?: 1;
    $coords = [];
    foreach ($valores as $i => $v) {
        $coords[] = round($m + $i * ($W - 2 * $m) / ($n - 1), 1) . ',' . round($H - $m - ((float)$v - $min) / $rango * ($H - 2 * $m), 1);
    }
    [$ux, $uy] = explode(',', end($coords));
    return '<svg class="spark" viewBox="0 0 ' . $W . ' ' . $H . '" preserveAspectRatio="none" aria-hidden="true">'
        . '<polyline points="' . implode(' ', $coords) . '" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" vector-effect="non-scaling-stroke"/>'
        // ultimo punto: trazo de largo 0 con punta redonda, asi no se deforma al estirar la grafica
        . '<path d="M' . $ux . ' ' . $uy . 'h0" stroke="currentColor" stroke-width="8" stroke-linecap="round" vector-effect="non-scaling-stroke"/></svg>';
}

// $puntos: [etiqueta => valor]; $color es un valor CSS (p. ej. var(--c-hum)), por eso va en style
function svg_linea(array $puntos, string $color, int $decimales): string
{
    if (!$puntos) {
        return '<p class="sub" style="text-align:center;padding:40px 0">No hay registros para el periodo seleccionado.</p>';
    }
    $W = 1000; $H = 360; $izq = 64; $der = 20; $arr = 20; $aba = 44;
    $anchoUtil = $W - $izq - $der;
    $altoUtil = $H - $arr - $aba;
    [$min, $max, $paso] = escala_y(min($puntos), max($puntos));
    $y = fn($v) => $arr + $altoUtil - ($v - $min) / ($max - $min) * $altoUtil;
    $n = count($puntos);
    $x = fn($i) => $n === 1 ? $izq + $anchoUtil / 2 : $izq + 14 + $i * ($anchoUtil - 28) / ($n - 1);

    $svg = '<svg viewBox="0 0 ' . $W . ' ' . $H . '" role="img">';
    // rejilla y eje Y
    for ($v = $min; $v <= $max + $paso / 2; $v += $paso) {
        $yy = round($y($v), 1);
        $svg .= '<path class="rejilla" d="M' . $izq . ' ' . $yy . 'H' . ($W - $der) . '"/>';
        $svg .= '<text x="' . ($izq - 8) . '" y="' . ($yy + 4) . '" text-anchor="end">' . number_format($v, $paso < 1 ? 1 : 0, '.', '') . '</text>';
    }
    $svg .= '<path class="eje" d="M' . $izq . ' ' . $arr . 'V' . ($H - $aba) . 'H' . ($W - $der) . '"/>';
    // linea, puntos y etiquetas
    $coords = [];
    $i = 0;
    $cadaCuanto = (int)ceil($n / 16); // evita que se encimen las etiquetas del eje X
    // Con pocos puntos se rotulan todos; con muchos solo el maximo, el minimo y el ultimo
    $valores = array_values($puntos);
    $rotular = $n <= 12 ? range(0, $n - 1)
        : array_unique([array_search(max($valores), $valores), array_search(min($valores), $valores), $n - 1]);
    $marcas = '';
    foreach ($puntos as $etq => $v) {
        $xx = round($x($i), 1);
        $yy = round($y($v), 1);
        $coords[] = "$xx,$yy";
        $marcas .= '<circle cx="' . $xx . '" cy="' . $yy . '" r="4.5" style="fill:' . $color . '" stroke-width="2"><title>' . e($etq) . ': ' . number_format($v, $decimales, '.', '') . '</title></circle>';
        if (in_array($i, $rotular, true)) {
            // en un valle la etiqueta va debajo del punto para no tapar la linea
            $valle = $v < ($valores[$i - 1] ?? INF) && $v <= ($valores[$i + 1] ?? INF);
            $marcas .= '<text x="' . $xx . '" y="' . ($valle ? $yy + 22 : $yy - 10) . '" text-anchor="middle" class="vl">' . number_format($v, $decimales, '.', '') . '</text>';
        }
        if ($n <= 16 || $i % $cadaCuanto === 0) {
            $marcas .= '<text x="' . $xx . '" y="' . ($H - $aba + 20) . '" text-anchor="middle">' . e($etq) . '</text>';
        }
        $i++;
    }
    $svg .= '<polyline points="' . implode(' ', $coords) . '" fill="none" style="stroke:' . $color . '" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>';
    return $svg . $marcas . '</svg>';
}
