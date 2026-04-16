package ru.practicum.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class UserAdminParam {
    private int from;
    private int size;
    private List<Long> ids;

    public UserAdminParam(int from, int size, List<Long> ids) {
        this.from = from;
        this.size = size;
        this.ids = ids;
    }
}
