<?php

namespace App\Filament\Resources\EmergencyContactResource\Pages;

use App\Filament\Resources\EmergencyContactResource;
use Filament\Actions;
use Filament\Resources\Pages\ManageRecords;

class ManageEmergencyContacts extends ManageRecords
{
    protected static string $resource = EmergencyContactResource::class;

    protected function getHeaderActions(): array
    {
        return [
            Actions\CreateAction::make(),
        ];
    }
}
