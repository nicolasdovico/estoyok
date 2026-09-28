<?php

use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return response()->json([
        'service' => 'Estoy Ok API',
        'status' => 'operational',
    ]);
});
