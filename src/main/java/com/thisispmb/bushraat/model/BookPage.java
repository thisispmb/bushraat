package com.thisispmb.bushraat.model;

import java.util.List;

public record BookPage(List<Book> books, boolean hasMore) {
}