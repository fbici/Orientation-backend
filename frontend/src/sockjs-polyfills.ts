// SockJS 1.x browser utilities reference the Node-compatible global alias.
(globalThis as typeof globalThis & { global: typeof globalThis }).global ??= globalThis;
