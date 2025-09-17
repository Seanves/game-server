package net.seanv.stonegameserver.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.security.Principal;

@AllArgsConstructor
public class PrincipalImpl implements Principal {

    @Getter
    private final int id;
    // ...

    @Override
    public String getName() {
        return Integer.toString(id);
    }

}
