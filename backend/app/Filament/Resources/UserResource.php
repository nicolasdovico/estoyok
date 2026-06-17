<?php

namespace App\Filament\Resources;

use App\Filament\Resources\UserResource\Pages;
use App\Models\User;
use Filament\Forms;
use Filament\Forms\Form;
use Filament\Resources\Resource;
use Filament\Tables;
use Filament\Tables\Table;

class UserResource extends Resource
{
    protected static ?string $model = User::class;

    protected static ?string $navigationIcon = 'heroicon-o-rectangle-stack';

    public static function form(Form $form): Form
    {
        return $form
            ->schema([
                Forms\Components\TextInput::make('name')->required(),
                Forms\Components\TextInput::make('email')->email()->required(),
                Forms\Components\TextInput::make('phone'),
                Forms\Components\DateTimePicker::make('last_check_in_at'),
                Forms\Components\Toggle::make('is_premium'),
                Forms\Components\Toggle::make('allow_sms_whatsapp_checkin'),
                Forms\Components\TextInput::make('expo_push_token'),
                Forms\Components\Section::make('Subscriptions')
                    ->schema([
                        Forms\Components\TextInput::make('mp_subscription_id'),
                        Forms\Components\TextInput::make('mp_status'),
                        Forms\Components\TextInput::make('paypal_subscription_id'),
                        Forms\Components\TextInput::make('paypal_status'),
                    ])->columns(2),
            ]);
    }

    public static function table(Table $table): Table
    {
        return $table
            ->columns([
                Tables\Columns\TextColumn::make('name')->searchable(),
                Tables\Columns\TextColumn::make('email')->searchable(),
                Tables\Columns\TextColumn::make('phone'),
                Tables\Columns\TextColumn::make('last_check_in_at')->dateTime()->sortable(),
                Tables\Columns\IconColumn::make('is_premium')->boolean(),
                Tables\Columns\IconColumn::make('allow_sms_whatsapp_checkin')->boolean(),
                Tables\Columns\TextColumn::make('mp_status')->label('MP Status')->badge(),
                Tables\Columns\TextColumn::make('paypal_status')->label('PayPal Status')->badge(),
                Tables\Columns\TextColumn::make('created_at')->dateTime()->sortable()->toggleable(isToggledHiddenByDefault: true),
            ])
            ->filters([
                Tables\Filters\TernaryFilter::make('is_premium'),
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
            'index' => Pages\ManageUsers::route('/'),
        ];
    }
}
