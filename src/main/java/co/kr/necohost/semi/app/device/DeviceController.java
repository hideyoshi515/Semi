package co.kr.necohost.semi.app.device;

import co.kr.necohost.semi.app.menu.MenuController;
import co.kr.necohost.semi.domain.model.dto.DeviceRequest;
import co.kr.necohost.semi.domain.model.entity.Device;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.service.MenuService;
import co.kr.necohost.semi.domain.service.DeviceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DeviceController {
    private final MenuService menuService;
    private final DeviceService deviceService;

    public DeviceController(MenuService menuService, DeviceService deviceService) {
        this.menuService = menuService;
        this.deviceService = deviceService;
    }

    @GetMapping("/order")
    public String order(Model model) {
        model.addAttribute("deviceRequest", new DeviceRequest());
        List<Device> device = deviceService.findAll();
        model.addAttribute("devices", device);
        return "order/orderInput.html";
    }

    @PostMapping("/order")
    public String postOrder(DeviceRequest deviceRequest, Model model, @RequestParam Map<String, Object> params) {
        model.addAttribute("deviceRequest", deviceRequest);
//        long device = Long.parseLong(params.get("device").toString());
        List<Menu> menus = menuService.getAllMenus();
        model.addAttribute("menus", menus);
        return "order/orderKioskInput.html";
    }

    @PostMapping("/orderPayment")
    public String checkout(@RequestParam Map<String, String> quantities, Model model) {
        Map<Menu, Integer> orderedItems = new HashMap<>();
        double totalPrice = 0;

        for (Map.Entry<String, String> entry : quantities.entrySet()) {
            if (entry.getKey().startsWith("quantities[")) {
                String idStr = entry.getKey().substring(11, entry.getKey().length() - 2);
                Long id = Long.parseLong(idStr);
                Menu menu = menuService.getMenuById(id);
                int quantity = Integer.parseInt(entry.getValue());
                if (quantity > 0) {
                    orderedItems.put(menu, quantity);
                    totalPrice += menu.getPrice() * quantity;
                }
            }
        }

        model.addAttribute("orderedItems", orderedItems);
        model.addAttribute("totalPrice", totalPrice);

        return "order/orderPaymentInput.html";
    }
}