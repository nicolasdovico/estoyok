'use client';

import { useState, useEffect, Suspense } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';

function VerifyEmailForm() {
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const router = useRouter();
  const searchParams = useSearchParams();
  const email = searchParams.get('email');

  useEffect(() => {
    if (!email) {
      router.push('/register');
    }
  }, [email, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    setMessage('');

    if (code.length !== 6) {
      setError('El código debe ser de 6 dígitos');
      setIsLoading(false);
      return;
    }

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/verify-email`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({ 
          email, 
          code 
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Código inválido o expirado');
      }

      localStorage.setItem('auth_token', data.token);
      router.push('/');
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Ocurrió un error inesperado');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = async () => {
    setIsResending(true);
    setError('');
    setMessage('');

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/resend-otp`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({ email }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Error al reenviar código');
      }

      setMessage('Se ha enviado un nuevo código a tu email');
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Error al conectar con el servidor');
      }
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="bg-white py-8 px-4 shadow sm:rounded-2xl sm:px-10 border border-gray-100">
      <div className="mb-6 text-center">
        <p className="text-sm text-gray-600">
          Hemos enviado un código de verificación a:
          <br />
          <span className="font-bold text-gray-900">{email}</span>
        </p>
      </div>

      <form className="space-y-6" onSubmit={handleSubmit}>
        {error && (
          <div className="bg-red-50 border-l-4 border-red-400 p-4">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        {message && (
          <div className="bg-green-50 border-l-4 border-green-400 p-4">
            <p className="text-sm text-green-700">{message}</p>
          </div>
        )}

        <div>
          <label htmlFor="code" className="block text-sm font-medium text-gray-700 text-center">
            Código de 6 dígitos
          </label>
          <div className="mt-2">
            <input
              id="code"
              type="text"
              required
              maxLength={6}
              value={code}
              onChange={(e) => setCode(e.target.value.replace(/\D/g, ''))}
              className="appearance-none block w-full px-3 py-4 border border-gray-300 rounded-xl shadow-sm placeholder-gray-400 focus:outline-none focus:ring-red-500 focus:border-red-500 text-center text-2xl font-bold tracking-[0.5em]"
              placeholder="000000"
            />
          </div>
        </div>

        <div>
          <button
            type="submit"
            disabled={isLoading}
            className={`w-full flex justify-center py-3 px-4 border border-transparent rounded-xl shadow-sm text-sm font-bold text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 ${isLoading ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            {isLoading ? 'Verificando...' : 'Verificar Email'}
          </button>
        </div>
      </form>

      <div className="mt-6 flex flex-col items-center space-y-4">
        <button
          onClick={handleResend}
          disabled={isResending}
          className="text-sm font-medium text-red-600 hover:text-red-500 disabled:opacity-50"
        >
          {isResending ? 'Enviando...' : '¿No recibiste el código? Reenviar'}
        </button>
        
        <Link href="/register" className="text-xs text-gray-500 hover:text-gray-700">
          Volver al registro
        </Link>
      </div>
    </div>
  );
}

export default function VerifyEmailPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <Link href="/" className="flex justify-center mb-6">
          <span className="text-3xl font-black text-red-600 tracking-tighter">ESTOY OK</span>
        </Link>
        <h2 className="text-center text-3xl font-extrabold text-gray-900">
          Verifica tu cuenta
        </h2>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <Suspense fallback={<div className="text-center">Cargando...</div>}>
          <VerifyEmailForm />
        </Suspense>
      </div>
    </div>
  );
}
