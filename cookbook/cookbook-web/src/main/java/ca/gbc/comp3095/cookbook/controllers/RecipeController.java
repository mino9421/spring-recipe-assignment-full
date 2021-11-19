/*********************************************************************************
 * Project: Cookbook App
 * Assignment: COMP3095 Assignment1
 * Author(s): Chi Calvin Nguyen, Simon Ung, Deniz Dogan
 * Student Number: 101203877, 101032525, 101269485
 * Date: 2021-11-06
 * Description: RecipeController displays pages in the /recipes subdirectory.
 * RecipeController manages the application processes while the user is logged in
 *********************************************************************************/
package ca.gbc.comp3095.cookbook.controllers;

import ca.gbc.comp3095.cookbook.model.Meal;
import ca.gbc.comp3095.cookbook.model.Recipe;
import ca.gbc.comp3095.cookbook.model.User;
import ca.gbc.comp3095.cookbook.services.MealService;
import ca.gbc.comp3095.cookbook.services.RecipeService;
import ca.gbc.comp3095.cookbook.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.*;

@RequestMapping("/recipes") // Map controller to /recipes path in URL
@Controller // Annotates this class as a Controller to be managed by Spring Boot
public class RecipeController {

    // Dependencies
    private final RecipeService recipeService;
    private final UserService userService;
    private final MealService mealService;
    private HttpSession newSession;

    // Constructor Dependency Injection
    public RecipeController(RecipeService recipeService, UserService userService, MealService mealService) {
        this.recipeService = recipeService;
        this.userService = userService;
        this.mealService = mealService;
        this.newSession = null;
    }

    private boolean newSessionCheck(){
        return newSession == null;
    }

    @RequestMapping({"","/", "/index.html"})
    public String index(HttpSession session) {

        // Checks if there is a user attribute in session and checks if it is in the database
        // If it is true -> newSession will be the current session
        // (Checks if newSession is null in other methods of controller)

        if ((session.getAttribute("user") != null) &&
                (userService.checkCredentials((User) session.getAttribute("user")))) {

            newSession = session;
            return "/recipes/index";
        } else {
            return "redirect:/users/login";
        }
    }

    @RequestMapping({"/profile"})
    public String profile(Model model) {

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            // Get user
            User tempUser = (User) newSession.getAttribute("user");
            tempUser = userService.findByUsername(tempUser.getUsername());

            // get planned meals of user
            Set<Meal> mealSet = mealService.findMeals(tempUser.getId());

            // Get recipes created by user
            Set<Recipe> userRecipeSet = recipeService.findByUser(tempUser.getId());

            // Get favorite recipes of user
            Set<Recipe> favRecipeSet = recipeService.findByFavUser(tempUser.getId());

            model.addAttribute("users", tempUser);
            model.addAttribute("userRecipes", userRecipeSet);
            model.addAttribute("favRecipes", favRecipeSet);
            model.addAttribute("mealSet", mealSet);
            return "/recipes/profile";
        }
    }

    @RequestMapping({"/view-ingredients"})
    public String viewIngredients(@RequestParam Long id, Model model){

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            Recipe tempRecipe = recipeService.findById(id);
            model.addAttribute("recipe", tempRecipe);
            return "/recipes/view-ingredients";
        }
    }
    @RequestMapping({"/view-steps"})
    public String viewSteps(@RequestParam Long id, Model model){

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            Recipe tempRecipe = recipeService.findById(id);
            model.addAttribute("recipe", tempRecipe);
            return "/recipes/view-steps";
        }
    }

    @PostMapping({"/addtofavorites"})
    public String addToFavorites(@RequestParam Long recipeId) {

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            if(recipeId == -1L) {
                return "redirect:/recipes/profile";
            } else {
                Recipe tempRecipe = recipeService.findById(recipeId);
                User tempUser = (User) newSession.getAttribute("user");
                tempUser = userService.findByUsername(tempUser.getUsername());

                // Commit Into Database
                Set<Recipe> tempRecipeSet = tempUser.getFavoriteRecipes();
                Set<User> tempUserSet = tempRecipe.getFav_users();
                tempRecipeSet.add(tempRecipe);
                tempUserSet.add(tempUser);
                tempUser.setFavoriteRecipes(tempRecipeSet);
                tempRecipe.setFav_users(tempUserSet);
                userService.save(tempUser);
                recipeService.save(tempRecipe);

                return "redirect:/recipes/profile";
            }
        }
    }

    @RequestMapping("createRecipe")
    public String createRecipe(Model model) {

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            model.addAttribute("recipe", new Recipe());
            return "/recipes/create-recipe";
        }
    }

    @RequestMapping("processRecipe")
    public String processRecipe(Recipe recipe) {

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            // Get Current User
            User tempUser = (User) newSession.getAttribute("user");
            tempUser = userService.findByUsername(tempUser.getUsername());

            // Set the Recipes Author
            recipe.setUser(tempUser);
            recipe.setCreationDate(new Date());

            // Save Recipe to Database
            recipeService.save(recipe);
            return "redirect:/recipes/profile";
        }
    }

    @RequestMapping("/viewRecipe")
    public String viewRecipe(Model model) {

        if (newSessionCheck()){
            return "redirect:/users/login";
        } else {
            model.addAttribute("recipes", recipeService.findAll());
            return "/recipes/view-recipe";
        }
    }

    @RequestMapping("/searchRecipe")
    public String searchRecipe(@RequestParam(required = false) String key, Model model) {

        if (newSessionCheck()){
            return "redirect:/users/login";
        } else {
            if (key == null) {
                Set<Recipe> recipeSet = Collections.emptySet();
                model.addAttribute("recipeSet", recipeSet);
            } else {
                key = key.toLowerCase();
                Set<Recipe> recipeSet = recipeService.findByKeyword(key);
                model.addAttribute("recipeSet", recipeSet);
            }
            return "/recipes/search-recipe";
        }
    }

    @RequestMapping({"/logout"})
    public String logout() {
        newSession = null;
        return "redirect:/users/logout";
    }

    @RequestMapping("/planMeal")
    public String planMeal(@RequestParam(required = false) Long recipeId, Model model) {

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            Recipe tempRecipe = recipeService.findById(recipeId);
            if (tempRecipe.getId() == -1L) {
                return "redirect:/recipes/viewRecipe";
            } else {
                Date[] arrayDate = new Date[8];
                Date curDate = new Date();
                Calendar c = Calendar.getInstance();

                for (int i = 0; i < 8; i++){
                    c.setTime(curDate);
                    c.add(Calendar.DATE, i);
                    Date newDate = c.getTime();
                    arrayDate[i] = newDate;
                }

                model.addAttribute("recipe", tempRecipe);
                model.addAttribute("arrayDate", arrayDate);
                return "/recipes/plan-meal";
            }
        }
    }

    @PostMapping("/processMeal")
    public String processMeal(@RequestParam Long recipeId, Long addedDate){

        if (newSessionCheck()) {
            return "redirect:/users/login";
        } else {
            Meal tempMeal = new Meal();

            User tempUser = (User) newSession.getAttribute("user");
            tempUser = userService.findByUsername(tempUser.getUsername());
            Date plannedDate = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(plannedDate);
            c.add(Calendar.DATE, Math.toIntExact(addedDate));
            plannedDate = c.getTime();

            tempMeal.setMeal_recipe(recipeService.findById(recipeId));
            tempMeal.setMeal_user(tempUser);
            tempMeal.setMeal_date(plannedDate);
            mealService.save(tempMeal);
            return "redirect:/recipes/profile";
        }
    }
}
