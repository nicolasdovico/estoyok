<?php

namespace App\Filament\Resources\CircleResource\Pages;

use App\Filament\Resources\CircleResource;
use Filament\Actions;
use Filament\Resources\Pages\ManageRecords;

class ManageCircles extends ManageRecords
{
    protected static string $resource = CircleResource::class;

    protected function getHeaderActions(): array
    {
        return [
            Actions\CreateAction::make(),
        ];
    }
}
