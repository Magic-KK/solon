package demo.nodes;

import com.yomahub.liteflow.core.NodeComponent;
import org.noear.solon.annotation.Component;

/**
 * @author noear 2022/9/21 created
 */
@Component("b")
public class BCmp extends NodeComponent {

    @Override
    public void process() {
        //do your business
        System.out.println(this.getClass().getName());
    }
}

