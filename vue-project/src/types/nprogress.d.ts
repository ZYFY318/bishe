declare module 'nprogress' {
  interface NProgress {
    start(): void;
    done(): void;
    set(n: number): void;
    inc(amount?: number): void;
    configure(options: NProgressOptions): void;
    status: number | null;
    remove(): void;
  }

  interface NProgressOptions {
    minimum?: number;
    template?: string;
    easing?: string;
    speed?: number;
    trickle?: boolean;
    trickleSpeed?: number;
    showSpinner?: boolean;
    parent?: string;
    positionUsing?: string;
    barSelector?: string;
    spinnerSelector?: string;
  }

  const nprogress: NProgress;
  export = nprogress;
} 