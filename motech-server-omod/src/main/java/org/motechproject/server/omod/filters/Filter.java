package org.motechproject.server.omod.filters;


import java.util.List;

public interface Filter<E> {
    List<E> on(List<E> items);
}
