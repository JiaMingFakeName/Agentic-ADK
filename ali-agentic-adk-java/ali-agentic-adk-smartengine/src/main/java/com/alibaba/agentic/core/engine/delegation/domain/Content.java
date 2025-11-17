package com.alibaba.agentic.core.engine.delegation.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/9/16 16:18
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Content implements Serializable {

    private static final long serialVersionUID = 4618596179660800203L;
    private Type type;

    private String text;

    private String imageUrl;


    public enum Type {
        text,
        image
    }
}
