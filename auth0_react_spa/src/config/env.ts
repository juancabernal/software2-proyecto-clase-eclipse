const readEnv = (key: string): string | undefined => {
  const rawValue = (import.meta.env as Record<string, string | undefined>)[key];
  return rawValue?.trim() || undefined;
};

const ensure = (key: string, value: string | undefined): string => {
  if (!value) {
    throw new Error(
      `[config] Missing environment variable ${key}. Revisa tu archivo .env (puedes copiar .env.example).`
    );
  }
  return value;
};

const fallbackCallback = () => {
  if (typeof window !== "undefined") {
    console.warn(
      "[config] VITE_AUTH0_CALLBACK_URL no está definido. Usando window.location.origin/callback como valor por defecto."
    );
    return `${window.location.origin}/callback`;
  }
  return "/callback";
};

const scope = readEnv("VITE_AUTH0_SCOPE") || "read:users read:settings";

export const env = {
  api: {
    baseUrl: ensure("VITE_API_SERVER_URL", readEnv("VITE_API_SERVER_URL")),
  },
  auth0: {
    domain: ensure("VITE_AUTH0_DOMAIN", readEnv("VITE_AUTH0_DOMAIN")),
    clientId: ensure("VITE_AUTH0_CLIENT_ID", readEnv("VITE_AUTH0_CLIENT_ID")),
    audience: ensure("VITE_AUTH0_AUDIENCE", readEnv("VITE_AUTH0_AUDIENCE")),
    callbackUrl: readEnv("VITE_AUTH0_CALLBACK_URL") || fallbackCallback(),
    scope,
  },
} as const;

export type AppEnv = typeof env;
