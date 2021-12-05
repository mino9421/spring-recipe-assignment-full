package ca.gbc.comp3095.cookbook.controllers;

import ca.gbc.comp3095.cookbook.model.Event;
import ca.gbc.comp3095.cookbook.model.User;
import ca.gbc.comp3095.cookbook.services.EventService;
import ca.gbc.comp3095.cookbook.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Set;

@RequestMapping("/events")
@Controller
public class EventController {
    // Dependencies
    private final EventService eventService;
    private final UserService userService;
    private HttpSession newSession;

    // Constructor Dependency Injection
    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    private boolean newSessionCheck(){
        return newSession == null;
    }

    @RequestMapping({"", "/", "index"})
    public String index(Model model, HttpSession session) {

        if ((session.getAttribute("user") != null) &&
                (userService.checkCredentials((User) session.getAttribute("user")))) {

            newSession = session;

            User tempUser = userService.findByUsername(((User) newSession.getAttribute("user")).getUsername());
            Set<Event> tempEventSet = eventService.findAllByUserId(tempUser.getId());

            model.addAttribute("events", tempEventSet);
            return "/events/index";

        } else {
            return "redirect:/users/login";
        }
    }

    @RequestMapping("/createEvent")
    public String createEvent(Model model) {

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            model.addAttribute("event", new Event());
            return "/events/create-event";
        }
    }

    @RequestMapping("processEvent")
    public String processEvent(Event event) {

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            User tempUser = userService.findByUsername(((User) newSession.getAttribute("user")).getUsername());

            event.setEventUser(tempUser);
            eventService.save(event);

            return "redirect:/events/";
        }
    }

    @RequestMapping("/updateEvent/{eventId}")
    public String updateEvent(@PathVariable Long eventId, Model model) {

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {

            Event tempEvent = eventService.findById(eventId);
            model.addAttribute("currentEvent", tempEvent);
            System.out.println(tempEvent.getEventUser());

            return "/events/update-event";
        }
    }

    @RequestMapping("/processUpdate")
    public String processUpdate(Event event) {

        System.out.println(event.getEventUser());

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            eventService.save(event);
            return "redirect:/events/";
        }
    }


    @RequestMapping("/deleteEvent")
    public String deleteEvent(Long eventId) {

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            eventService.deleteById(eventId);
            return "redirect:/events/";
        }
   }
}


