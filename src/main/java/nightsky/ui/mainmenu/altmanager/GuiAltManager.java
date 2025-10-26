package nightsky.ui.mainmenu.altmanager;

import me.liuli.elixir.account.CrackedAccount;
import me.liuli.elixir.account.MicrosoftAccount;
import me.liuli.elixir.account.MinecraftAccount;
import me.liuli.elixir.account.MojangAccount;
import nightsky.ui.mainmenu.altmanager.SessionUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;
import nightsky.ui.mainmenu.altmanager.menus.GuiLoginIntoAccount;
import nightsky.ui.mainmenu.altmanager.menus.GuiSessionLogin;
import org.lwjgl.input.Keyboard;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GuiAltManager extends GuiScreen {

    private GuiScreen prevGui;
    public String status = "§7Idle...";

    private GuiButton loginButton;
    private GuiButton randomAltButton;
    private GuiButton randomNameButton;
    private GuiButton addButton;
    private GuiButton removeButton;
    private GuiButton copyButton;
    private GuiList altsList;
    private GuiTextField searchField;

    public GuiAltManager(GuiScreen prevGui) {
        this.prevGui = prevGui;
    }

    @Override
    public void initGui() {
        int textFieldWidth = Math.max(width / 8, 70);
        searchField = new GuiTextField(2, mc.fontRendererObj, width - textFieldWidth - 10, 10, textFieldWidth, 20);
        searchField.setMaxStringLength(Integer.MAX_VALUE);

        altsList = new GuiList(this);
        altsList.registerScrollButtons(7, 8);

        int currentAccountIndex = -1;
        for (int i = 0; i < AccountsConfig.getInstance().getAccounts().size(); i++) {
            if (AccountsConfig.getInstance().getAccounts().get(i).getName().equals(mc.getSession().getUsername())) {
                currentAccountIndex = i;
                break;
            }
        }
        if (currentAccountIndex != -1) {
            altsList.elementClicked(currentAccountIndex, false, 0, 0);
            altsList.scrollBy(currentAccountIndex * altsList.getSlotHeight());
        }

        int startPositionY = 22;
        addButton = new GuiButton(1, width - 80, startPositionY + 24, 70, 20, "Add");
        removeButton = new GuiButton(2, width - 80, startPositionY + 24 * 2, 70, 20, "Remove");
        buttonList.add(new GuiButton(13, width - 80, startPositionY + 24 * 3, 70, 20, "Move Up"));
        buttonList.add(new GuiButton(14, width - 80, startPositionY + 24 * 4, 70, 20, "Move Down"));
        buttonList.add(new GuiButton(7, width - 80, startPositionY + 24 * 5, 70, 20, "Import"));
        buttonList.add(new GuiButton(12, width - 80, startPositionY + 24 * 6, 70, 20, "Export"));
        copyButton = new GuiButton(8, width - 80, startPositionY + 24 * 7, 70, 20, "Copy");

        buttonList.add(new GuiButton(0, width - 80, height - 65, 70, 20, "Back"));
        loginButton = new GuiButton(3, 5, startPositionY + 24, 90, 20, "Login");
        randomAltButton = new GuiButton(4, 5, startPositionY + 24 * 2, 90, 20, "Random Alt");
        randomNameButton = new GuiButton(5, 5, startPositionY + 24 * 3, 90, 20, "Random Name");
        buttonList.add(new GuiButton(6, 5, startPositionY + 24 * 4, 90, 20, "Direct Login"));
        buttonList.add(new GuiButton(10, 5, startPositionY + 24 * 5, 90, 20, "Session Login"));
        buttonList.add(new GuiButton(11, 5, startPositionY + 24 * 7, 90, 20, "Reload"));

        buttonList.add(addButton);
        buttonList.add(removeButton);
        buttonList.add(loginButton);
        buttonList.add(randomAltButton);
        buttonList.add(randomNameButton);
        buttonList.add(copyButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);
        altsList.drawScreen(mouseX, mouseY, partialTicks);
        
        drawCenteredString(fontRendererObj, "Alt Manager", width / 2, 6, 0xffffff);
        
        String accountText = searchField.getText().isEmpty() ? 
            AccountsConfig.getInstance().getAccounts().size() + " Alts" : 
            altsList.getAccounts().size() + " Search Results";
        drawCenteredString(fontRendererObj, accountText, width / 2, 18, 0xffffff);
        drawCenteredString(fontRendererObj, status, width / 2, 32, 0xffffff);
        
        fontRendererObj.drawStringWithShadow("§7User: §a" + mc.getSession().getUsername(), 6, 6, 0xffffff);

        searchField.drawTextBox();
        if (searchField.getText().isEmpty() && !searchField.isFocused()) {
            fontRendererObj.drawStringWithShadow("§7Search...", searchField.xPosition + 4, 17, 0xffffff);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;

        switch (button.id) {
            case 0:
                mc.displayGuiScreen(prevGui);
                break;
            case 1:
                mc.displayGuiScreen(new GuiLoginIntoAccount(this));
                break;
            case 2:
                if (altsList.selectedSlot != -1 && altsList.selectedSlot < altsList.getSize()) {
                    AccountsConfig.getInstance().removeAccount(altsList.getAccounts().get(altsList.selectedSlot));
                    AccountsConfig.getInstance().saveConfig();
                    status = "§aThe account has been removed.";
                } else {
                    status = "§cSelect an account.";
                }
                break;
            case 3:
                MinecraftAccount selectedAccount = altsList.getSelectedAccount();
                if (selectedAccount != null) {
                    loginButton.enabled = false;
                    randomAltButton.enabled = false;
                    randomNameButton.enabled = false;
                    
                    login(selectedAccount, () -> {
                        status = "§aLogged into §f§l" + mc.getSession().getUsername() + "§a.";
                    }, (exception) -> {
                        status = "§cLogin failed due to '" + exception.getMessage() + "'.";
                    }, () -> {
                        loginButton.enabled = true;
                        randomAltButton.enabled = true;
                        randomNameButton.enabled = true;
                    });
                    
                    status = "§aLogging in...";
                } else {
                    status = "§cSelect an account.";
                }
                break;
            case 4:
                List<MinecraftAccount> accounts = altsList.getAccounts();
                if (!accounts.isEmpty()) {
                    MinecraftAccount randomAccount = accounts.get(new Random().nextInt(accounts.size()));
                    loginButton.enabled = false;
                    randomAltButton.enabled = false;
                    randomNameButton.enabled = false;
                    
                    login(randomAccount, () -> {
                        status = "§aLogged into §f§l" + mc.getSession().getUsername() + "§a.";
                    }, (exception) -> {
                        status = "§cLogin failed due to '" + exception.getMessage() + "'.";
                    }, () -> {
                        loginButton.enabled = true;
                        randomAltButton.enabled = true;
                        randomNameButton.enabled = true;
                    });
                    
                    status = "§aLogging in...";
                } else {
                    status = "§cYou do not have any accounts.";
                }
                break;
            case 5:
                String randomName = generateRandomName();
                SessionUtils.setSession(new Session(randomName, "", "", "legacy"));
                status = "§aLogged into §f§l" + randomName + "§a.";
                break;
            case 6:
                mc.displayGuiScreen(new GuiLoginIntoAccount(this, true));
                break;
            case 7:
                importAccounts();
                break;
            case 8:
                copySelectedAccount();
                break;
            case 10:
                mc.displayGuiScreen(new GuiSessionLogin(this));
                break;
            case 11:
                AccountsConfig.getInstance().loadConfig();
                status = "§aReloaded accounts.";
                break;
            case 12:
                exportAccounts();
                break;
            case 13:
                moveAccountUp();
                break;
            case 14:
                moveAccountDown();
                break;
        }
    }

    private void importAccounts() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
            
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                List<String> lines = Files.readAllLines(file.toPath());
                
                for (String line : lines) {
                    String[] accountData = line.split(":", 2);
                    if (accountData.length > 1) {
                        AccountsConfig.getInstance().addMojangAccount(accountData[0], accountData[1]);
                    } else if (accountData[0].length() < 16) {
                        AccountsConfig.getInstance().addCrackedAccount(accountData[0]);
                    }
                }
                
                AccountsConfig.getInstance().saveConfig();
                status = "§aThe accounts were imported successfully.";
            }
        } catch (Exception e) {
            status = "§cFailed to import accounts: " + e.getMessage();
        }
    }

    private void exportAccounts() {
        if (AccountsConfig.getInstance().getAccounts().isEmpty()) {
            status = "§cYou do not have any accounts to export.";
            return;
        }

        try {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file.isDirectory()) return;

                if (!file.exists()) {
                    file.createNewFile();
                }

                StringBuilder accounts = new StringBuilder();
                for (MinecraftAccount account : AccountsConfig.getInstance().getAccounts()) {
                    if (account instanceof MojangAccount) {
                        MojangAccount mojang = (MojangAccount) account;
                        accounts.append(mojang.getEmail()).append(":").append(mojang.getPassword());
                    } else if (account instanceof MicrosoftAccount) {
                        MicrosoftAccount microsoft = (MicrosoftAccount) account;
                        accounts.append(microsoft.getName()).append(":").append(microsoft.getSession().getToken());
                    } else {
                        accounts.append(account.getName());
                    }
                    accounts.append("\n");
                }

                Files.write(file.toPath(), accounts.toString().getBytes());
                status = "§aExported successfully!";
            }
        } catch (Exception e) {
            status = "§cUnable to export due to error: " + e.getMessage();
        }
    }

    private void copySelectedAccount() {
        MinecraftAccount currentAccount = altsList.getSelectedAccount();
        if (currentAccount == null) {
            status = "§cSelect an account.";
            return;
        }

        try {
            String formattedData;
            if (currentAccount instanceof MojangAccount) {
                MojangAccount mojang = (MojangAccount) currentAccount;
                formattedData = mojang.getEmail() + ":" + mojang.getPassword();
            } else if (currentAccount instanceof MicrosoftAccount) {
                MicrosoftAccount microsoft = (MicrosoftAccount) currentAccount;
                formattedData = microsoft.getName() + ":" + microsoft.getSession().getToken();
            } else {
                formattedData = currentAccount.getName();
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new java.awt.datatransfer.StringSelection(formattedData), null);
            status = "§aCopied account into your clipboard.";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveAccountUp() {
        MinecraftAccount currentAccount = altsList.getSelectedAccount();
        if (currentAccount == null) {
            status = "§cSelect an account.";
            return;
        }
        
        List<MinecraftAccount> accounts = AccountsConfig.getInstance().getAccounts();
        int currentIndex = accounts.indexOf(currentAccount);
        if (currentIndex == 0) return;
        
        Collections.swap(accounts, currentIndex - 1, currentIndex);
        AccountsConfig.getInstance().saveConfig();
        altsList.selectedSlot--;
    }

    private void moveAccountDown() {
        MinecraftAccount currentAccount = altsList.getSelectedAccount();
        if (currentAccount == null) {
            status = "§cSelect an account.";
            return;
        }
        
        List<MinecraftAccount> accounts = AccountsConfig.getInstance().getAccounts();
        int currentIndex = accounts.indexOf(currentAccount);
        if (currentIndex == accounts.size() - 1) return;
        
        Collections.swap(accounts, currentIndex, currentIndex + 1);
        AccountsConfig.getInstance().saveConfig();
        altsList.selectedSlot++;
    }

    private String generateRandomName() {
        String[] names = {"Player", "User", "Gamer", "Pro", "Epic", "Cool", "Super", "Mega", "Ultra", "Best"};
        String[] suffixes = {"123", "456", "789", "2024", "Pro", "Gaming", "MC", "PvP", "God", "King"};
        Random random = new Random();
        return names[random.nextInt(names.length)] + suffixes[random.nextInt(suffixes.length)];
    }

    private void login(MinecraftAccount account, Runnable success, java.util.function.Consumer<Exception> error, Runnable done) {
        new Thread(() -> {
            try {
                account.update();
                SessionUtils.setSession(new Session(
                    account.getSession().getUsername(),
                    account.getSession().getUuid(),
                    account.getSession().getToken(),
                    "microsoft"
                ));
                success.run();
            } catch (Exception exception) {
                error.accept(exception);
            }
            done.run();
        }).start();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField.isFocused()) {
            searchField.textboxKeyTyped(typedChar, keyCode);
        }

        switch (keyCode) {
            case Keyboard.KEY_ESCAPE:
                mc.displayGuiScreen(prevGui);
                break;
            case Keyboard.KEY_UP:
                altsList.selectedSlot -= 1;
                break;
            case Keyboard.KEY_DOWN:
                altsList.selectedSlot += 1;
                break;
            case Keyboard.KEY_TAB:
                altsList.selectedSlot += Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? -1 : 1;
                break;
            case Keyboard.KEY_RETURN:
                altsList.elementClicked(altsList.selectedSlot, true, 0, 0);
                break;
            case Keyboard.KEY_NEXT:
                altsList.scrollBy(height - 100);
                break;
            case Keyboard.KEY_PRIOR:
                altsList.scrollBy(-height + 100);
                break;
            case Keyboard.KEY_ADD:
                actionPerformed(addButton);
                break;
            case Keyboard.KEY_DELETE:
            case Keyboard.KEY_MINUS:
                actionPerformed(removeButton);
                break;
            case Keyboard.KEY_C:
                if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    actionPerformed(copyButton);
                } else {
                    super.keyTyped(typedChar, keyCode);
                }
                break;
            default:
                super.keyTyped(typedChar, keyCode);
                break;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        altsList.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        searchField.updateCursorCounter();
    }

    private class GuiList extends GuiSlot {
        public int selectedSlot = 0;

        public GuiList(GuiScreen prevGui) {
            super(GuiAltManager.this.mc, prevGui.width, prevGui.height, 40, prevGui.height - 40, 30);
        }

        public List<MinecraftAccount> getAccounts() {
            String search = searchField.getText();
            if (search == null || search.isEmpty()) {
                return AccountsConfig.getInstance().getAccounts();
            }
            search = search.toLowerCase();

            List<MinecraftAccount> filtered = new ArrayList<>();
            for (MinecraftAccount account : AccountsConfig.getInstance().getAccounts()) {
                if (account.getName().toLowerCase().contains(search) || 
                    (account instanceof MojangAccount && ((MojangAccount) account).getEmail().toLowerCase().contains(search))) {
                    filtered.add(account);
                }
            }
            return filtered;
        }

        public void setSelectedSlot(int value) {
            if (getAccounts().isEmpty()) return;
            this.selectedSlot = ((value % getAccounts().size()) + getAccounts().size()) % getAccounts().size();
        }

        public int getSelectedSlot() {
            return selectedSlot >= getAccounts().size() ? -1 : selectedSlot;
        }

        public MinecraftAccount getSelectedAccount() {
            List<MinecraftAccount> accounts = getAccounts();
            return selectedSlot >= 0 && selectedSlot < accounts.size() ? accounts.get(selectedSlot) : null;
        }

        @Override
        protected boolean isSelected(int id) {
            return selectedSlot == id;
        }

        @Override
        protected int getSize() {
            return getAccounts().size();
        }

        @Override
        protected void elementClicked(int clickedElement, boolean doubleClick, int var3, int var4) {
            selectedSlot = clickedElement;

            if (doubleClick) {
                MinecraftAccount selectedAccount = getSelectedAccount();
                if (selectedAccount != null) {
                    loginButton.enabled = false;
                    randomAltButton.enabled = false;
                    randomNameButton.enabled = false;

                    login(selectedAccount, () -> {
                        status = "§aLogged into §f§l" + mc.getSession().getUsername() + "§a.";
                    }, (exception) -> {
                        status = "§cLogin failed due to '" + exception.getMessage() + "'.";
                    }, () -> {
                        loginButton.enabled = true;
                        randomAltButton.enabled = true;
                        randomNameButton.enabled = true;
                    });

                    status = "§aLogging in...";
                } else {
                    status = "§cSelect an account.";
                }
            }
        }

        @Override
        protected void drawSlot(int id, int x, int y, int var4, int var5, int var6) {
            MinecraftAccount minecraftAccount = getAccounts().get(id);
            String accountName = minecraftAccount.getName();
            if (minecraftAccount instanceof MojangAccount && accountName.isEmpty()) {
                accountName = ((MojangAccount) minecraftAccount).getEmail();
            }

            fontRendererObj.drawStringWithShadow(accountName, width / 2f - 40, y + 2, Color.WHITE.getRGB());
            
            String accountType;
            int color;
            if (minecraftAccount instanceof CrackedAccount) {
                accountType = "Cracked";
                color = Color.GRAY.getRGB();
            } else if (minecraftAccount instanceof MicrosoftAccount) {
                accountType = "Microsoft";
                color = new Color(118, 255, 95).getRGB();
            } else if (minecraftAccount instanceof MojangAccount) {
                accountType = "Mojang";
                color = new Color(118, 255, 95).getRGB();
            } else {
                accountType = "Something else";
                color = Color.GRAY.getRGB();
            }
            
            fontRendererObj.drawStringWithShadow(accountType, width / 2f, y + 15, color);
        }

        @Override
        protected void drawBackground() {}
    }
}
