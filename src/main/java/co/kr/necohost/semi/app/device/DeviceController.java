package co.kr.necohost.semi.app.device;

import co.kr.necohost.semi.app.menu.MenuController;
import co.kr.necohost.semi.domain.model.dto.AccountRequest;
import co.kr.necohost.semi.domain.model.dto.DeviceRequest;
import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.model.entity.Device;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.service.AccountService;
import co.kr.necohost.semi.domain.service.MenuService;
import co.kr.necohost.semi.domain.service.DeviceService;
import co.kr.necohost.semi.domain.service.SalesService;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DeviceController {
    private final MenuService menuService;
    private final DeviceService deviceService;
    private final SalesService salesService;
    private final MenuRepository menuRepository;
    private final AccountService accountService;

    public DeviceController(MenuService menuService, DeviceService deviceService, SalesService salesService, MenuRepository menuRepository, AccountService accountService ) {
        this.menuService = menuService;
        this.deviceService = deviceService;
        this.salesService = salesService;
        this.menuRepository = menuRepository;
        this.accountService = accountService;
    }
    private int calculatePoints(long price){
        return (int)(price * 0.01);
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public String getOrder(Model model) {
        model.addAttribute("deviceRequest", new DeviceRequest());
        List<Device> device = deviceService.findAll();
        model.addAttribute("devices", device);
        return "order/orderOption.html";
    }

    @RequestMapping(value = "/order",method = RequestMethod.POST)
    public String postOrder(DeviceRequest deviceRequest, Model model, @RequestParam Map<String, Object> params) {
        model.addAttribute("deviceRequest", deviceRequest);
//        long device = Long.parseLong(params.get("device").toString());
        Map<Long, List<Menu>> categorizedMenus = menuService.getCategorizedMenus();
        model.addAttribute("categorizedMenus", categorizedMenus);
        List<Menu> menus = menuService.getAllMenus();
        model.addAttribute("menus", menus);
        return "order/orderMenuSelect.html";
    }

    @RequestMapping(value = "/orderPayment", method = RequestMethod.POST)
    public String postOrderPayment(@RequestParam Map<String, String> quantities, Model model,DeviceRequest deviceRequest) {
        Map<Menu, Integer> orderedItems = new HashMap<>();
        long totalPrice = 0;

        for (Map.Entry<String, String> entry : quantities.entrySet()) {
            if (entry.getKey().startsWith("quantities[")) {
                String idStr = entry.getKey().substring(11, entry.getKey().length() - 1);
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

        return "order/orderPaymentSelect.html";
    }

    @RequestMapping(value = "/orderPaymentSelect",method = RequestMethod.POST)
    public String OrderPaymentSelect(@ModelAttribute DeviceRequest deviceRequest, @RequestParam String paymentMethod, Model model, SalesRequest salesRequest, AccountRequest accountRequest) {
        System.out.println("DeviceRequest: " + deviceRequest);
        Map<Long, Integer> quantities = deviceRequest.getQuantities();
        long totalPrice = 0;
        String phone = deviceRequest.getPhoneNum();
        String pass = deviceRequest.getPass();




        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Long menuId = entry.getKey();
            Integer quantity = entry.getValue();
            Menu menu = menuService.getMenuById(menuId);
            Account account = accountService.getAccountByPhone(phone);

            if (quantity != null && quantity > 0 && quantity <= menu.getStock()) {
                totalPrice += menu.getPrice() * quantity;
                LocalDateTime localDate = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = localDate.format(formatter);


                accountRequest = new AccountRequest();
                salesRequest = new SalesRequest();
                salesRequest.setCategory((int) menu.getCategory());
                salesRequest.setDate(LocalDateTime.parse(formattedDateTime, formatter));
                salesRequest.setMenu((int) menu.getId());
                salesRequest.setQuantity(quantity);
                salesRequest.setDevice((int) deviceRequest.getDevice());
                salesRequest.setDeviceNum((int) deviceRequest.getDeviceNum());
                salesRequest.setOrderNum((int) deviceRequest.getOrderNum());
                salesRequest.setProcess(paymentMethod.equals("CASH") ? 1 : paymentMethod.equals("CARD") ? 2 : 3);

                salesService.save(salesRequest);

                menu.setStock(menu.getStock() - quantity);
                menuService.saveMenu(menu);



                int pointsEarned = calculatePoints(menu.getPrice() * quantity);
                if (phone.equals(phone)){
                    account.setMsPoint(account.getMsPoint() + pointsEarned);
                    accountService.save(accountRequest);
                }else {
                   return "order/orderPoint.html";
                }


            }
        }
        model.addAttribute("orderedItems", quantities);
        model.addAttribute("totalPrice", totalPrice);

        switch (paymentMethod) {
            case "CASH":
                return "order/orderPayment.html";
            case "CARD":
                return "order/orderPayment.html";
            case "PAY":
                return "order/orderPayment.html";
            default:
                model.addAttribute("error", "유효하지 않은 결제방법입니다.");
                return "order/orderPaymentSelect.html";
        }


    }
}