export function authError(error: any, fallback: string): string {
 if(error?.name==='TimeoutError' || error?.status===0) return 'La connexion est indisponible ou trop lente. Veuillez réessayer.';
 if(error?.status===429) return 'Trop de tentatives. Patientez avant de réessayer.';
 if(error?.status===503) return 'Ce service est momentanément indisponible. Veuillez réessayer plus tard.';
 return fallback;
}
export function validEmail(value:string): boolean { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim()); }
export function validPassword(value:string): boolean { return value.length>=12 && new TextEncoder().encode(value).length<=72; }
export function safeReturnUrl(value:unknown):string {
 if(typeof value!=='string' || !/^\/(?![\/\\])/.test(value) || /[\\\x00-\x20]/.test(value) || value.startsWith('/auth')) return '/dashboard';
 return value;
}
