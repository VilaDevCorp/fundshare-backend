package com.viladev.fundshare.forms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SearchGroupForm extends SearchBaseForm {

    private String keyword;
    private String userId;

}
