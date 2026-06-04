<?php

namespace App;

use OpenApi\Attributes as OA;

#[OA\Info(
    title: "Estoy Ok API",
    version: "1.0.0",
    description: "API para la plataforma de seguridad familiar Estoy Ok",
    contact: new OA\Contact(email: "soporte@estoyok.com")
)]
#[OA\Server(
    url: "/api",
    description: "Servidor API Principal"
)]
class SwaggerInfo
{
}
