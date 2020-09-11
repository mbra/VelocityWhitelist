package org.wontfix.velocitywhitelist;

public class UuidResolverRecord {
    String name;
    String id;

    /* For some weird reason, Mojang returns a trimmed UUID from the profile api
   even though Javas UUID class only wants full (with the dashes) UUIDs.

   We should probably look for a library that wraps the Mojang API and
   check if that is doing this stuff for us already.

   But then again I'd need to understand all the jar bundling stuff which
   I don't think I am ready for, yet. :-(
 */
    public String getFullUuid() {
        return this.id.replaceFirst(
                "(\\p{XDigit}{7})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$0-$2-$3-$4-$5"
        );
    }
}
