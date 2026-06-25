if (typeof window === 'undefined') {
  if (typeof global !== 'undefined' && !('localStorage' in global)) {
    Object.defineProperty(global, 'localStorage', {
      value: {
        getItem: () => null,
        setItem: () => {},
        removeItem: () => {},
        clear: () => {},
        key: () => null,
        length: 0,
      },
      writable: true,
      configurable: true,
    });
  }
}
