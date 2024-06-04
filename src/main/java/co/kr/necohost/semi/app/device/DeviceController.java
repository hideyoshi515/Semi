package co.kr.necohost.semi.app.device;

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
    @GetMapping("/orderPayment")
    public String orderPayment(Model model) {
        model.addAttribute("deviceRequest", new DeviceRequest());
        List<Menu> menus = menuService.getAllMenus();
        model.addAttribute("menus", menus);
        return "order/orderPaymentInput.html";
    }




}