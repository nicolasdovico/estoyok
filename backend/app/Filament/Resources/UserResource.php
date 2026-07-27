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
                Forms\Components\Toggle::make('is_premium')
                    ->label('Socio Premium (Acceso Total)')
                    ->live()
                    ->afterStateUpdated(function ($state, Forms\Set $set) {
                        if (! $state) {
                            $set('subscription_status', 'inactive');
                            $set('trial_ends_at', null);
                        } else {
                            $set('subscription_status', 'active');
                        }
                    }),
                Forms\Components\Toggle::make('allow_sms_whatsapp_checkin'),
                Forms\Components\TextInput::make('expo_push_token'),
                Forms\Components\Section::make('Suscripción y Prueba (Stripe / Mercado Pago / PayPal)')
                    ->schema([
                        Forms\Components\Select::make('subscription_status')
                            ->label('Estado de Suscripción')
                            ->options([
                                'inactive' => 'Inactivo / Gratis',
                                'trialing' => 'Prueba Gratis (7 Días)',
                                'active' => 'Activo (Suscrito)',
                                'canceled' => 'Cancelado',
                                'grace_period' => 'Período de Gracia',
                            ])
                            ->default('inactive')
                            ->live()
                            ->afterStateUpdated(function ($state, Forms\Set $set) {
                                if (in_array($state, ['inactive', 'canceled'])) {
                                    $set('is_premium', false);
                                    $set('trial_ends_at', null);
                                } elseif (in_array($state, ['active', 'trialing'])) {
                                    $set('is_premium', true);
                                }
                            }),
                        Forms\Components\Select::make('subscription_provider')
                            ->label('Pasarela de Pago')
                            ->options([
                                'stripe' => 'Stripe (Tarjeta)',
                                'mercadopago' => 'Mercado Pago',
                                'paypal' => 'PayPal',
                            ]),
                        Forms\Components\DateTimePicker::make('trial_ends_at')->label('Fin de Prueba Gratuita'),
                        Forms\Components\DateTimePicker::make('billing_cycle_ends_at')->label('Fin del Período de Facturación'),
                        Forms\Components\TextInput::make('mp_subscription_id')->label('MP Subscription ID'),
                        Forms\Components\TextInput::make('mp_status')->label('MP Status'),
                        Forms\Components\TextInput::make('paypal_subscription_id')->label('PayPal Subscription ID'),
                        Forms\Components\TextInput::make('paypal_status')->label('PayPal Status'),
                    ])->columns(2),
            ]);
    }

    public static function table(Table $table): Table
    {
        return $table
            ->columns([
                Tables\Columns\TextColumn::make('name')->searchable(),
                Tables\Columns\TextColumn::make('email')->searchable(),
                Tables\Columns\IconColumn::make('is_premium')->boolean()->label('Premium'),
                Tables\Columns\TextColumn::make('subscription_status')
                    ->label('Estado Suscripción')
                    ->badge()
                    ->color(fn (string $state): string => match ($state) {
                        'active' => 'success',
                        'trialing' => 'warning',
                        'canceled' => 'danger',
                        'grace_period' => 'info',
                        default => 'gray',
                    }),
                Tables\Columns\TextColumn::make('subscription_provider')->label('Pasarela')->badge(),
                Tables\Columns\TextColumn::make('trial_ends_at')->dateTime()->label('Fin Prueba')->sortable(),
                Tables\Columns\TextColumn::make('phone'),
                Tables\Columns\TextColumn::make('last_check_in_at')->dateTime()->sortable(),
                Tables\Columns\TextColumn::make('created_at')->dateTime()->sortable()->toggleable(isToggledHiddenByDefault: true),
            ])
            ->filters([
                Tables\Filters\TernaryFilter::make('is_premium'),
                Tables\Filters\SelectFilter::make('subscription_status')
                    ->options([
                        'inactive' => 'Inactivo / Gratis',
                        'trialing' => 'Prueba Gratis (7 Días)',
                        'active' => 'Activo (Suscrito)',
                        'canceled' => 'Cancelado',
                    ]),
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
