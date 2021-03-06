/**
 * A safe finalize example that relies on piggybacking of a volatile write and read.
 *
 * <p>The use of the volatile field ensures a reference to "this" is held until the
 * end of the work method. Since the field is volatile, the instruction can not be
 * reordered. In addition, the field is copied to a public static field during
 * finalization to prevent a theoretical optimizer from eliminating the field.</p>
 *
 * <p>The advantage of this approach is that does not involve any form of locking.
 * It does, however, require a StoreLoad barrier after every pre-finalization
 * method call.</p>
 */
public final class SafeFinalizeVolatileFieldExample {
    public static int STATIC_COUNTER = 0;

    // Initialize with current static field value for an additional optimizer safe-guard
    private volatile int counter = STATIC_COUNTER;

    public void work() throws Exception {
        try {
            System.err.println("Work starting");
            System.gc();

            // Do some work here, potentially blocking (simulate with sleep)
            Thread.sleep(10000L);
            System.err.println("Work completed");
        } finally {
            counter++;  // program order rule, prevents reordering of this call
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();

        // Copy value to a public static for an additional optimizer safe-guard
        // Only needs to be done IF the finalizer is freeing the resource, if the user properly freed
        // the resource it can be skipped.
        STATIC_COUNTER = counter;
        System.err.println("Finalize");
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 2; i++) {
            System.out.println("Run " + (i + 1));
            new SafeFinalizeVolatileFieldExample().work();
            System.gc();
            Thread.sleep(2000);
        }
    }
}
