package online.hengtian.memory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpirePage {
    private long exprieTime=5000;
    private Page page;

}
