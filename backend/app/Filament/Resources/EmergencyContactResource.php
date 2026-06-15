<?php

namespace App\Filament\Resources;

use App\Filament\Resources\EmergencyContactResource\Pages;
use App\Models\EmergencyContact;
use Filament\Forms;
use Filament\Forms\Form;
use Filament\Resources\Resource;
use Filament\Tables;
use Filament\Tables\Table;

class EmergencyContactResource extends Resource
{
    protected static ?string $model = EmergencyContact::class;

    protected static ?string $navigationIcon = 'heroicon-o-phone';

    public static function form(Form $form): Form
    {
        return $form
            ->schema([
                Forms\Components\Select::make('user_id')
                    ->relationship('user', 'name')
                    ->required()
                    ->searchable(),
                Forms\Components\TextInput::make('name')->required(),
                Forms\Components\TextInput::make('phone')->tel()->required(),
                Forms\Components\TextInput::make('email')->email(),
            ]);
    }

    public static function table(Table $table): Table
    {
        return $table
            ->columns([
                Tables\Columns\TextColumn::make('user.name')->label('User')->searchable(),
                Tables\Columns\TextColumn::make('name')->searchable(),
                Tables\Columns\TextColumn::make('phone'),
                Tables\Columns\TextColumn::make('email'),
                Tables\Columns\TextColumn::make('created_at')->dateTime()->sortable(),
            ])
            ->filters([
                //
            ])
            ->actions([
                Tables\Actions\EditAction::make(),
                Tables\Actions\DeleteAction::make(),
            ])
            ->bulkActions([
                Tables\Actions\BulkActionGroup::make([
                    Tables\Actions\DeleteBulkAction::make(),
                ]),
            ]);
    }

    public static function getPages(): array
    {
        return [
            'index' => Pages\ManageEmergencyContacts::route('/'),
        ];
    }
}
